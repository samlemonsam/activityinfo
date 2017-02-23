package org.activityinfo.ui.client.input.model;

import org.activityinfo.model.form.FormField;

public class FieldValueModel {

    private FormField field;
    private InputModel input;

    public FieldValueModel(FormField field) {
        this.field = field;
        this.input = field.getType().accept(InputModelFactory.INSTANCE);
    }

    public FormField getField() {
        return field;
    }




}
