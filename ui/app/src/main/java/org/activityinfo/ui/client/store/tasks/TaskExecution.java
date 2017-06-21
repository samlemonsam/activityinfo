package org.activityinfo.ui.client.store.tasks;

import org.activityinfo.ui.client.store.http.HttpBus;
import org.activityinfo.ui.client.store.http.HttpRequest;

/**
 * A reference to a {@link HttpRequest} submitted to the {@link HttpBus} that can be used
 * to cancel the request.
 */
public interface TaskExecution {

    boolean isRunning();

    void cancel();

}
