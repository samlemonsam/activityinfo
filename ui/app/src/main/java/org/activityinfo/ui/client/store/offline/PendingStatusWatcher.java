package org.activityinfo.ui.client.store.offline;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import org.activityinfo.ui.client.store.http.RefetchHandler;
import org.activityinfo.ui.client.store.tasks.Watcher;

/**
 * Watches for changes to the pending transaction queue.
 */
class PendingStatusWatcher implements Watcher {

    private final EventBus eventBus;
    private HandlerRegistration registration;

    public PendingStatusWatcher(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void start(RefetchHandler handler) {
        registration = eventBus.addHandler(PendingStatusEvent.TYPE, event -> handler.refetch());
    }

    @Override
    public void stop() {
        registration.removeHandler();
        registration = null;
    }
}
