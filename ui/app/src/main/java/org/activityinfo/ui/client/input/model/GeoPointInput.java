package org.activityinfo.ui.client.input.model;

import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.promise.BiFunction;

public class GeoPointInput extends InputModel {

    private StatefulValue<String> latitudeInput;
    private StatefulValue<String> longitudeInput;
    private Observable<InputValue> value;

    public GeoPointInput() {
        latitudeInput = new StatefulValue<>("");
        longitudeInput = new StatefulValue<>("");
        value = Observable.transform(latitudeInput, longitudeInput, new BiFunction<String, String, InputValue>() {
            @Override
            public InputValue apply(String latitude, String longitude) {
                return InputValue.invalid("TODO");
            }
        });
    }

    @Override
    public Observable<InputValue> getValue() {
        return value;
    }
}
