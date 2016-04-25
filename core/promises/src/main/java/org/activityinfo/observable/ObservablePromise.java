package org.activityinfo.observable;

import org.activityinfo.promise.Promise;

/**
 * Created by yuriyz on 4/25/2016.
 */
public class ObservablePromise<T> extends Observable<T> {

    private final Promise<T> promise;

    public ObservablePromise(Promise<T> promise) {
        this.promise = promise;
    }

    @Override
    public boolean isLoading() {
        return !promise.isSettled();
    }

    @Override
    public T get() {
        return promise.get();
    }

    public Promise<T> getPromise() {
        return promise;
    }
}
