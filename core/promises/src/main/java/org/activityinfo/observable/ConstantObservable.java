package org.activityinfo.observable;

import org.activityinfo.observable.Observable;

public class ConstantObservable<T> extends Observable<T> {
    
    private final T value;

    public ConstantObservable(T value) {
        this.value = value;
    }

    @Override
    public boolean isLoading() {
        return false;
    }

    @Override
    public T get() {
        return value;
    }
}
