package org.activityinfo.ui.client.store.tasks;

import org.activityinfo.ui.client.store.http.HttpStore;
import org.activityinfo.ui.client.store.http.HttpRequest;

/**
 * A reference to a {@link HttpRequest} submitted to the {@link HttpStore} that can be used
 * to cancel the request.
 */
public interface TaskExecution {

    boolean isRunning();

    void cancel();

}
