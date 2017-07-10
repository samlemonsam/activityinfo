package org.activityinfo.ui.client.store.tasks;

public class NullWatcher implements Watcher {

    public static final NullWatcher INSTANCE = new NullWatcher();

    private NullWatcher() {}

    @Override
    public void start(RefetchHandler handler) {
    }

    @Override
    public void stop() {

    }
}
