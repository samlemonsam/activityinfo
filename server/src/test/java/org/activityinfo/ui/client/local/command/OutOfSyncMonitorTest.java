/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.local.command;

import org.activityinfo.ui.client.MockEventBus;
import org.activityinfo.ui.client.local.sync.SyncCompleteEvent;
import org.activityinfo.ui.client.local.sync.SyncRequestEvent;
import org.activityinfo.ui.client.local.sync.SyncStatusEvent;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class OutOfSyncMonitorTest {

    private MockEventBus eventBus;
    private OutOfSyncMonitor.View notifier;

    @Before
    public void setUp() {
        eventBus = new MockEventBus();
        notifier = createNiceMock(OutOfSyncMonitor.View.class);
        replay(notifier);
    }

    @Test
    public void simple() {

        OutOfSyncMonitor monitor = new OutOfSyncMonitor(eventBus, notifier);
        monitor.onServerStateMutated();

        // The notifier should mark its state as dirty
        assertThat("outOfSync", monitor.isOutOfSync(), equalTo(true));

        // Synchronization should be triggered
        eventBus.assertEventFired(SyncRequestEvent.INSTANCE);

        syncStatusEventFires(0);
        syncStatusEventFires(50);
        syncStatusEventFires(95);

        // When synchronization completes, the dirty flag should be changed
        syncCompleteEventFires();

        assertThat("outOfSync", monitor.isOutOfSync(), equalTo(false));
    }

    private boolean syncCompleteEventFires() {
        return eventBus.fireEvent(new SyncCompleteEvent(new Date()));
    }

    @Test
    public void notFireOnStartup() {

        new OutOfSyncMonitor(eventBus, notifier);

        syncCompleteEventFires();

        eventBus.assertNotFired(SyncRequestEvent.TYPE);

    }

    @Test
    public void syncAlreadyInProgress() {

        OutOfSyncMonitor monitor = new OutOfSyncMonitor(eventBus, notifier);

        // sync is already started...
        syncStatusEventFires(50);

        // ...when a new mutation is sent
        monitor.onServerStateMutated();

        // The notifier should mark its state as dirty
        assertThat("outOfSync", monitor.isOutOfSync(), equalTo(true));

        syncCompleteEventFires();

        assertThat("outOfSync", monitor.isOutOfSync(), equalTo(true));

        // Synchronization should be triggered
        // ...but should not trigger a new round of synchronization until
        // the previous synchronization is complete

        eventBus.assertEventFired(SyncRequestEvent.INSTANCE);

        syncStatusEventFires(0);
        syncStatusEventFires(50);
        syncStatusEventFires(95);
        syncCompleteEventFires();

        assertThat("outOfSync", monitor.isOutOfSync(), equalTo(false));
    }

    private void syncStatusEventFires(int percentComplete) {
        eventBus.fireEvent(new SyncStatusEvent("", percentComplete));
    }

}
