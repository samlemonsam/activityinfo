package org.activityinfo.ui.client.store.tasks;

public interface Watcher {

    void start(RefetchHandler handler);

    void stop();
}
