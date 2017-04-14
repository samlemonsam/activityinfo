package org.activityinfo.ui.client.input.view.field;

import org.activityinfo.model.resource.ResourceId;

public class FieldView {
    private final ResourceId fieldId;
    private final FieldWidget widget;

    public FieldView(ResourceId fieldId, FieldWidget widget) {
        this.fieldId = fieldId;
        this.widget = widget;
    }

    public ResourceId getFieldId() {
        return fieldId;
    }

    public FieldWidget getWidget() {
        return widget;
    }
}
