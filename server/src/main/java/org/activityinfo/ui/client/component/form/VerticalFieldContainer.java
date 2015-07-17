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

    public VerticalFieldContainer(FormField formField, FormFieldWidget fieldWidget) {
        this.field = formField;
        this.fieldWidget = fieldWidget;
        formGroup = new FormGroup()
                .label(formField.getLabel())
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