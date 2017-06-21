package org.activityinfo.ui.client.store.tasks;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.promise.Promise;

/**
 * A {@link TaskExecution} implementation that provides a simple mechanism
 * for cancelling tasks.
 *
 * <p>If the execution is cancelled, the execution's callback will not be invoked.</p>
 */
class CancellableExecution<T> implements TaskExecution {

    private final Promise<T> result;

    private boolean cancelled = false;

    public CancellableExecution(Promise<T> result, AsyncCallback<T> callback) {
        this.result = result;
        this.result.then(new AsyncCallback<T>() {
            @Override
            public void onFailure(Throwable caught) {
                if(!cancelled) {
                    callback.onFailure(caught);
                }
            }

            @Override
            public void onSuccess(T result) {
                if(!cancelled) {
                    callback.onSuccess(result);
                }
            }
        });
    }

    @Override
    public boolean isRunning() {
        return !result.isSettled();
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

}
