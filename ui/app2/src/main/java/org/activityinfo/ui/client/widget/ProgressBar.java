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
package org.activityinfo.ui.client.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.i18n.shared.I18N;

public class ProgressBar implements IsWidget {


    @UiField DivElement barElement;
    @UiField SpanElement screenReaderText;

    private final HTMLPanel rootElement;

    interface ProgressBarUiBinder extends UiBinder<HTMLPanel, ProgressBar> {
    }

    private static ProgressBarUiBinder ourUiBinder = GWT.create(ProgressBarUiBinder.class);

    public ProgressBar() {
        rootElement = ourUiBinder.createAndBindUi(this);
    }

    @Override
    public Widget asWidget() {
        return rootElement;
    }

    /**
     *
     * @param percentComplete a percentage between 0 and 100
     */
    public void setValue(int percentComplete) {
        assert percentComplete >= 0 && percentComplete <= 100;
        barElement.getStyle().setWidth(percentComplete, Style.Unit.PCT);
        barElement.setAttribute("aria-valuenow", Integer.toString(percentComplete));
        screenReaderText.setInnerText(I18N.MESSAGES.percentComplete(percentComplete));
    }

}