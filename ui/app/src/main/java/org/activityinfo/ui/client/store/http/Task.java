package org.activityinfo.ui.client.store.http;


import com.google.gwt.user.client.rpc.AsyncCallback;

public interface Task<T> {


    TaskExecution start(AsyncCallback<T> callback);

    /**
     * Given the successful result {@code result}, returns the delay in milliseconds before the request
     * should be re-issued to check for updates.
     */
    int refreshInterval(T result);
}
