package org.activityinfo.ui.client.input.viewModel;

import org.activityinfo.model.type.FieldValue;

import java.util.Optional;

/**
 * Responsible for validating a field's value.
 */
public interface FieldValueValidator<T extends FieldValue> {

    /**
     * Validates an updated field value.
     *
     * @return an error message if the field is invalid.
     */
    Optional<String> validate(T value);
}
