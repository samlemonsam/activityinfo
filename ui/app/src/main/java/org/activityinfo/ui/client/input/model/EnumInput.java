package org.activityinfo.ui.client.input.model;


import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;

import java.util.Optional;

public class EnumInput extends InputModel {

    private final Observable<InputValue> value;
    private StatefulValue<Optional<ResourceId>> selected;

    public EnumInput() {
        selected = new StatefulValue<>(Optional.empty());
        value = selected.transform(s -> {
            if(s.isPresent()) {
                return InputValue.valid(new EnumValue(s.get()));
            } else {
                return InputValue.empty();
            }
        });
    }

    @Override
    public Observable<InputValue> getValue() {
        return value;
    }
}
