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

import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.form.FormField;
import org.activityinfo.ui.client.component.form.field.FormFieldWidget;
import org.activityinfo.ui.client.widget.form.FormGroup;
import org.activityinfo.ui.client.widget.form.ValidationStateType;

/**
 * Simple field container which displays the label, input, and help text in a vertical
 * line.
 */
public class VerticalFieldContainer implements FieldContainer {

    public static class Factory implements FieldContainerFactory {
        @Override
        public FieldContainer createContainer(FormField field, FormFieldWidget widget, int columnWidth) {
            return new VerticalFieldContainer(field, widget);
        }
    }

    private final FormGroup formGroup;
    private final FormField field;
    private final FormFieldWidget fieldWidget;
    private boolean valid = true;

    public VerticalFieldContainer(FormField formField, FormFieldWidget fieldWidget) {
        this.field = formField;
        this.fieldWidget = fieldWidget;
        formGroup = new FormGroup()
                .label(field.getLabel())
                .description(formField.getDescription())
                .validationStateType(ValidationStateType.ERROR)
                .addWidget(fieldWidget);
    }

    @Override
    public FormField getField() {
        return field;
    }

    @Override
    public FormFieldWidget getFieldWidget() {
        return fieldWidget;
    }

    @Override
    public void setValid() {
        valid = true;
        formGroup.showValidationMessage(false);
    }

    @Override
    public void setInvalid(String message) {
        valid = false;
        formGroup.showValidationMessage(message);
    }

    @Override
    public boolean isInputValid() {
        return valid;
    }

    @Override
    public Widget asWidget() {
        return formGroup;
    }

}