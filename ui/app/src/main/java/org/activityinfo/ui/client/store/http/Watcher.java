package org.activityinfo.ui.client.store.http;

public interface Watcher {

    void start(RefetchHandler handler);

    void stop();
}
