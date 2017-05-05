package org.activityinfo.ui.client.store;

import com.google.gwt.core.client.testing.StubScheduler;


/**
 * Runs deferred tasks immediately.
 */
class ImmediateScheduler extends StubScheduler {

    @Override
    public void scheduleDeferred(ScheduledCommand cmd) {
        cmd.execute();
    }
}
