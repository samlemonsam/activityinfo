package org.activityinfo.ui.client.local.sync;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.EventType;
import org.activityinfo.ui.client.EventBus.NamedEventType;

public class SyncErrorEvent extends BaseEvent {

    public static final EventType TYPE = new NamedEventType("SyncError");

    private SyncErrorType type;

    public SyncErrorEvent(SyncErrorType type) {
        super(TYPE);
        this.type = type;
    }

    public SyncErrorType getErrorType() {
        return type;
    }
}
