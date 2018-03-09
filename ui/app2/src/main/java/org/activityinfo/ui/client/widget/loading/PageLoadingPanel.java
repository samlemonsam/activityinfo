/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.widget.loading;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.ui.client.widget.Button;
import org.activityinfo.ui.icons.Icons;

/**
 * Loading Panel view for top level views
 */
public class PageLoadingPanel implements IsWidget, LoadingPanelView {

    private final HTMLPanel rootElement;

    @UiField
    Element icon;

    @UiField
    Element heading;

    @UiField
    Element explanation;

    @UiField
    Button retryButton;

    @UiField
    SimplePanel content;


    interface PageLoadingUiBinder extends UiBinder<HTMLPanel, PageLoadingPanel> {
    }

    private static PageLoadingUiBinder ourUiBinder = GWT.create(PageLoadingUiBinder.class);

    public PageLoadingPanel() {
        rootElement = ourUiBinder.createAndBindUi(this);

        Icons.INSTANCE.ensureInjected();
        LoadingStylesheet.INSTANCE.ensureInjected();
    }

    @Override
    public void onLoadingStateChanged(LoadingState state, Throwable caught) {
        rootElement.setStyleName(LoadingStylesheet.INSTANCE.loading(), state == LoadingState.LOADING);
        rootElement.setStyleName(LoadingStylesheet.INSTANCE.failed(), state == LoadingState.FAILED);
        rootElement.setStyleName(LoadingStylesheet.INSTANCE.loaded(), state == LoadingState.LOADED);

        if(state == LoadingState.FAILED) {
            icon.setClassName(ExceptionOracle.getIcon(caught));
            heading.setInnerText(ExceptionOracle.getHeading(caught));
            explanation.setInnerText(ExceptionOracle.getExplanation(caught));
        }
    }

    @Override
    public Widget getWidget() {
        return content.getWidget();
    }

    @Override
    public void setWidget(Widget w) {
        onLoadingStateChanged(LoadingState.LOADED, null);
        content.setWidget(w);
    }

    public void setContentStyleName(String styleName) {
        content.setStyleName(styleName);
    }

    @Override
    public void setWidget(IsWidget widget) {
        setWidget(widget.asWidget());
    }

    @Override
    public HasClickHandlers getRetryButton() {
        return retryButton;
    }

    @Override
    public Widget asWidget() {
        return rootElement;
    }
}