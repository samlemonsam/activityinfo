package org.activityinfo.ui.client.store.offline;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import org.activityinfo.ui.client.store.tasks.Watcher;

public class PendingStatusEvent extends GwtEvent<PendingEventHandler> {
    public static Type<PendingEventHandler> TYPE = new Type<PendingEventHandler>();

    public Type<PendingEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(PendingEventHandler handler) {
        handler.onPendingQueueChanged(this);
    }

    public static Watcher watchFor(EventBus eventBus) {
        return new PendingStatusWatcher(eventBus);
    }

}
