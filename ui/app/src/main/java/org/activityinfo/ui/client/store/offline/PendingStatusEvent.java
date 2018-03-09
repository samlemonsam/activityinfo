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
package org.activityinfo.ui.client.store.offline;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import org.activityinfo.ui.client.store.tasks.Watcher;

/**
 * Signals that the status of pending updates has changed.
 */
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
