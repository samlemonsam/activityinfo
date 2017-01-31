package org.activityinfo.ui.client.component.form;
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

import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.form.FormField;
import org.activityinfo.ui.client.component.form.field.FormFieldWidget;
import org.activityinfo.ui.client.widget.form.FormGroup;
import org.activityinfo.ui.client.widget.form.ValidationStateType;

/**
 * @author yuriyz on 11/11/2014.
 */
public class HorizontalFieldContainer implements FieldContainer {

    public static class Factory implements FieldContainerFactory {
        @Override
        public FieldContainer createContainer(FormField field, FormFieldWidget widget, int columnWidth) {
            return new HorizontalFieldContainer(field, widget, columnWidth);
        }
    }

    private final FormGroup formGroup;
    private final FormField field;
    private final FormFieldWidget fieldWidget;

    public HorizontalFieldContainer(FormField formField, FormFieldWidget fieldWidget) {
        this(formField, fieldWidget, 4);
    }

    public HorizontalFieldContainer(FormField formField, FormFieldWidget fieldWidget, int columnWidth) {
        this.field = formField;
        this.fieldWidget = fieldWidget;
        formGroup = new FormGroup()
                .label(formField.getLabel())
                .description(formField.getDescription())
                .addWidget(fieldWidget)
                .validationStateType(ValidationStateType.ERROR)
                .columnLabelWidth(columnWidth);

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
        formGroup.showValidationMessage(false);
    }

    @Override
    public void setInvalid(String message) {
        formGroup.showValidationMessage(message);
    }

    @Override
    public Widget asWidget() {
        return formGroup;
    }

}