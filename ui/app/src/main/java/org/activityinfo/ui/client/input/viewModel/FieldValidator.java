package org.activityinfo.ui.client.input.viewModel;

import com.google.common.collect.Multimap;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;

import java.util.Optional;

public class FieldValidator {
    private ResourceId fieldId;
    private FieldValueValidator validator;

    public FieldValidator(ResourceId fieldId, FieldValueValidator validator) {
        this.fieldId = fieldId;
        this.validator = validator;
    }

    public void run(FormInstance record, Multimap<ResourceId, String> validationErrors) {
        FieldValue value = record.get(fieldId);
        if(value != null) {
            Optional<String> errorMessage = validator.validate(value);
            if(errorMessage.isPresent()) {
                validationErrors.put(fieldId, errorMessage.get());
            }
        }
    }
}
