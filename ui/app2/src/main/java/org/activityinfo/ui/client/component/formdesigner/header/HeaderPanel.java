package org.activityinfo.ui.client.component.formdesigner.header;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormElementContainer;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;
import org.activityinfo.ui.client.component.formdesigner.FormDesignerStyles;
import org.activityinfo.ui.client.component.formdesigner.container.FieldsHolder;
import org.activityinfo.ui.client.component.formdesigner.event.HeaderSelectionEvent;
import org.activityinfo.ui.client.component.formdesigner.event.WidgetContainerSelectionEvent;

/**
 * @author yuriyz on 7/11/14.
 */
public class HeaderPanel implements FieldsHolder, IsWidget {

    private static final OurUiBinder uiBinder = GWT.create(OurUiBinder.class);

    interface OurUiBinder extends UiBinder<FocusPanel, HeaderPanel> {
    }

    @UiField
    FocusPanel focusPanel;

    @UiField
    HeadingElement header;

    @UiField
    Label description;


    private FormDesigner formDesigner;
    private FormClass formClass;

    public HeaderPanel() {
        uiBinder.createAndBindUi(this);
    }

    public void start(FormDesigner formDesigner) {
        this.formDesigner = formDesigner;
        this.formClass = formDesigner.getRootFormClass();

        formDesigner.getEventBus().addHandler(WidgetContainerSelectionEvent.TYPE, new WidgetContainerSelectionEvent.Handler() {
            @Override
            public void handle(WidgetContainerSelectionEvent event) {
                setSelected(false);
            }
        });
        show();
    }

    @Override
    public Widget asWidget() {
        return focusPanel;
    }

    public FormClass getFormClass() {
        return formClass;
    }


    public void show() {
        header.setInnerText(Strings.nullToEmpty(formDesigner.getRootFormClass().getLabel()));
        description.setText(formDesigner.getRootFormClass().getDescription());
    }

    @UiHandler("focusPanel")
    void onClick(ClickEvent event) {
        formDesigner.getContainerPresenter().show(this);
        formDesigner.getFormDesignerPanel().setContainerPropertiesPanelVisible();

        formDesigner.getEventBus().fireEvent(new HeaderSelectionEvent(this));
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                setSelected(true);
            }
        });
    }

    public void setSelected(boolean selected) {
        if (selected) {
            focusPanel.addStyleName(FormDesignerStyles.INSTANCE.widgetContainerSelected());
        } else {
            focusPanel.removeStyleName(FormDesignerStyles.INSTANCE.widgetContainerSelected());
        }
    }

    @Override
    public FormElementContainer getElementContainer() {
        return formClass;
    }

    @Override
    public void updateUi() {
        show();
    }
}
