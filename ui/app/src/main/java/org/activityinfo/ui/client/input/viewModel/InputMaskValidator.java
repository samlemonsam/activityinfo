package org.activityinfo.ui.client.input.viewModel;

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.type.primitive.InputMask;
import org.activityinfo.model.type.primitive.TextValue;

import java.util.Optional;

public class InputMaskValidator implements FieldValueValidator<TextValue> {

    private InputMask inputMask;

    public InputMaskValidator(InputMask inputMask) {
        this.inputMask = inputMask;
    }

    public InputMaskValidator(String inputMask) {
        this(new InputMask(inputMask));
    }

    @Override
    public Optional<String> validate(TextValue value) {
        if(inputMask.isValid(value.asString())) {
            return Optional.empty();
        }
        return Optional.of(I18N.MESSAGES.invalidTextInput(inputMask.placeHolderText()));
    }
}
