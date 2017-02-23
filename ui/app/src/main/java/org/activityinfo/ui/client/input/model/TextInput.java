package org.activityinfo.ui.client.input.model;

import com.google.common.base.Strings;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;

public class TextInput extends InputModel {

    private StatefulValue<String> input;
    private Observable<InputValue> value;

    public TextInput() {
        input = new StatefulValue<>();
        value = input.transform(str -> {
            if(Strings.isNullOrEmpty(str)) {
                return InputValue.empty();
            } else {
                return InputValue.valid(createValue(str));
            }
        });
    }

    public Observable<String> getInput() {
        return input;
    }

    protected FieldValue createValue(String str) {
        return TextValue.valueOf(str);
    }

    @Override
    public Observable<InputValue> getValue() {
        return value;
    }

    public void update(String text) {
        input.updateIfNotEqual(text);
    }
}
