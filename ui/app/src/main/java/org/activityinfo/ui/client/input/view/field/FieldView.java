package org.activityinfo.ui.client.input.view.field;

import com.google.gwt.user.client.ui.HTML;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.input.viewModel.FormInputViewModel;

public class FieldView {
    private final ResourceId fieldId;
    private final FieldWidget widget;
    private HTML missingMessage;

    public FieldView(ResourceId fieldId, FieldWidget widget, HTML missingMessage) {
        this.fieldId = fieldId;
        this.widget = widget;
        this.missingMessage = missingMessage;
    }

    public ResourceId getFieldId() {
        return fieldId;
    }

    public FieldWidget getWidget() {
        return widget;
    }

    public void update(FormInputViewModel viewModel) {
        widget.setRelevant(viewModel.isRelevant(fieldId));
        missingMessage.setVisible(viewModel.isMissing(fieldId));
    }
}
