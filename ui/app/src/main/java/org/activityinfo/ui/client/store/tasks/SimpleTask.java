package org.activityinfo.ui.client.store.tasks;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.promise.Promise;

public abstract class SimpleTask<T> implements Task<T> {

    @Override
    public final TaskExecution start(AsyncCallback<T> callback) {
        return new CancellableExecution<>(execute(), callback);
    }

    protected abstract Promise<T> execute();

    @Override
    public int refreshInterval(T result) {
        return 0;
    }
}
