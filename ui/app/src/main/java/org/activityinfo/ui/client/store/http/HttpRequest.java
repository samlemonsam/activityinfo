package org.activityinfo.ui.client.store.http;

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.FormChange;


public interface HttpRequest<T> {

    Promise<T> execute(ActivityInfoClientAsync async);

    /**
     * Returns {@code true} if this request should be refetched from the server after the given
     */
    boolean shouldRefresh(FormChange change);

    /**
     * Given the successful result {@code result}, returns the delay in milliseconds before the request
     * should be re-issued to check for updates.
     */
    int refreshInterval(T result);

}
