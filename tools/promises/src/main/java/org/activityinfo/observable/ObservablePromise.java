package org.activityinfo.observable;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.promise.Promise;

/**
 * Created by yuriyz on 4/25/2016.
 */
public class ObservablePromise<T> extends Observable<T> {

    private final Promise<T> promise;

    public ObservablePromise(Promise<T> promise) {
        this.promise = promise;
        promise.then(new AsyncCallback<T>() {
            @Override
            public void onFailure(Throwable caught) {
                fireChange();
            }

            @Override
            public void onSuccess(T result) {
                fireChange();
            }
        });
    }

    @Override
    public boolean isLoading() {
        return !promise.isSettled();
    }

    @Override
    public T get() {
        if (promise.getState() == Promise.State.FULFILLED) {
            return promise.get();
        } else {
            return null;
        }
    }

    public Promise<T> getPromise() {
        return promise;
    }
}
