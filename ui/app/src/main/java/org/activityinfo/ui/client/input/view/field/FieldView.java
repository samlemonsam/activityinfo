package org.activityinfo.ui.client.input.view.field;

import com.google.gwt.user.client.ui.HTML;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.ui.client.input.viewModel.FormInputViewModel;

import java.util.Collection;

public class FieldView {
    private final ResourceId fieldId;
    private final FieldWidget widget;
    private HTML validationMessage;

    public FieldView(ResourceId fieldId, FieldWidget widget, HTML validationMessage) {
        this.fieldId = fieldId;
        this.widget = widget;
        this.validationMessage = validationMessage;
    }

    public ResourceId getFieldId() {
        return fieldId;
    }

    public FieldWidget getWidget() {
        return widget;
    }

    public void update(FormInputViewModel viewModel) {
        widget.setRelevant(viewModel.isRelevant(fieldId));

        if(viewModel.isMissing(fieldId)) {
            validationMessage.setText(I18N.CONSTANTS.requiredFieldMessage());
            validationMessage.setVisible(true);
        } else {
            Collection<String> validationErrors = viewModel.getValidationErrors(fieldId);
            if(!validationErrors.isEmpty()) {
                validationMessage.setText(validationErrors.iterator().next());
                validationMessage.setVisible(true);
            } else {
                validationMessage.setVisible(false);
            }
        }
    }

    public void init(FormInputViewModel viewModel) {
        FieldValue fieldValue = viewModel.getField(fieldId);
        if(fieldValue != null) {
            widget.init(fieldValue);
        }
    }
}

