package org.activityinfo.ui.client.component.form;

import org.activityinfo.model.form.FormField;
import org.activityinfo.ui.client.component.form.field.FormFieldWidget;

/**
 * Creates a FieldContainer appropriate for the form's context, which might vary
 * from a dialog box to a full page form to a mobile device.
 */
public interface FieldContainerFactory {

    public FieldContainer createContainer(FormField field, FormFieldWidget widget, int columnWidth);
}
