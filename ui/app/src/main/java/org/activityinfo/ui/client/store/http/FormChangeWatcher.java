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
package org.activityinfo.ui.client.store.http;

import com.google.common.base.Predicate;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import org.activityinfo.ui.client.store.FormChange;
import org.activityinfo.ui.client.store.FormChangeEvent;
import org.activityinfo.ui.client.store.tasks.RefetchHandler;
import org.activityinfo.ui.client.store.tasks.Watcher;


public class FormChangeWatcher implements Watcher {

    private final EventBus eventBus;

    private HandlerRegistration registration;

    private Predicate<FormChange> predicate;


    public FormChangeWatcher(EventBus eventBus, Predicate<FormChange> predicate) {
        this.eventBus = eventBus;
        this.predicate = predicate;
    }


    @Override
    public void start(RefetchHandler handler) {
        registration = eventBus.addHandler(FormChangeEvent.TYPE, event -> {
            if (predicate.test(event.getChange())) {
                // Our current result has become outdated, we need to fetch a new version from
                // the server
                handler.refetch();
            }
        });
    }

    @Override
    public void stop() {
        assert registration != null : "Watcher not started!";

        registration.removeHandler();
        registration = null;
    }
}
