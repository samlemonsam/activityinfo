package org.activityinfo.ui.client.store.tasks;

import org.activityinfo.ui.client.store.http.RefetchHandler;

public interface Watcher {

    void start(RefetchHandler handler);

    void stop();
}
