package org.activityinfo.ui.client.input.model;

import com.google.common.base.Strings;
import com.google.gwt.i18n.client.NumberFormat;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;

public class QuantityInput extends InputModel {

    private StatefulValue<String> input;
    private Observable<InputValue> value;

    public QuantityInput() {

        input = new StatefulValue<>("");
        value = input.transform(QuantityInput::parse);
    }

    @Override
    public Observable<InputValue> getValue() {
        return value;
    }

    private static InputValue parse(String string) {
        if(Strings.isNullOrEmpty(string)) {
            return InputValue.empty();
        } else {
            try {
                return InputValue.valid(new Quantity(NumberFormat.getDecimalFormat().parse(string)));
            } catch (NumberFormatException e) {
                return InputValue.invalid(e.getMessage());
            }
        }
    }


}
