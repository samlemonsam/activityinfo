package org.activityinfo.ui.client.store.offline;

import com.google.gwt.event.shared.EventHandler;

public interface PendingEventHandler extends EventHandler {
    void onPendingQueueChanged(PendingStatusEvent event);
}
