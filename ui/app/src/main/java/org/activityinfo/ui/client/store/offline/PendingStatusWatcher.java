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
import com.google.gwt.event.shared.HandlerRegistration;
import org.activityinfo.ui.client.store.tasks.RefetchHandler;
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
