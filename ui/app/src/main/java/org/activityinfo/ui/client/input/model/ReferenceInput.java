package org.activityinfo.ui.client.input.model;

import org.activityinfo.observable.Observable;


public class ReferenceInput extends InputModel {
    @Override
    public Observable<InputValue> getValue() {
        return Observable.just(InputValue.empty());
    }
}
