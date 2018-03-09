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
package org.activityinfo.ui.client.component.form;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.form.FormClass;

/**
 * Displays the Form class label as header
 */
public class FormHeading implements IsWidget {

    private static FormHeadingBinder uiBinder = GWT
            .create(FormHeadingBinder.class);
    private final HTMLPanel panel;
    @UiField
    HeadingElement headerElement;

    @Override
    public Widget asWidget() {
        return panel;
    }

    interface FormHeadingBinder extends UiBinder<HTMLPanel, FormHeading> {
    }

    public FormHeading() {
        panel = uiBinder.createAndBindUi(this);
    }
    
    public void setFormClass(FormClass formClass) {
        headerElement.setInnerText(formClass.getLabel());
    }
}
