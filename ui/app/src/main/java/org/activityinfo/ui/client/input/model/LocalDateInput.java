package org.activityinfo.ui.client.input.model;

import org.activityinfo.observable.Observable;

/**
 * Created by alex on 16-2-17.
 */
public class LocalDateInput extends InputModel {
    @Override
    public Observable<InputValue> getValue() {
        return Observable.just(InputValue.empty());
    }
}
