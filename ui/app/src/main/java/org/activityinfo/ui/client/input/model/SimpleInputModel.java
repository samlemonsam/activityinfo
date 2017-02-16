package org.activityinfo.ui.client.input.model;

import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;

/**
 * Created by alex on 16-2-17.
 */
public class SimpleInputModel extends InputModel {

    private StatefulValue<InputValue> value = new StatefulValue<>();

    @Override
    public Observable<InputValue> getValue() {
        return value;
    }
}
