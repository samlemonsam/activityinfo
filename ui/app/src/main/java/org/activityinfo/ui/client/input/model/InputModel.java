package org.activityinfo.ui.client.input.model;


import org.activityinfo.observable.Observable;

public abstract class InputModel {

    public abstract Observable<InputValue> getValue();

}
