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

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;
import org.activityinfo.ui.client.store.FormStoreImpl;
import org.activityinfo.ui.client.store.http.HttpStore;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Periodically compares the local state with the server and updates the local store.
 */
public class RecordSynchronizer {

    private static final Logger LOGGER = Logger.getLogger(RecordSynchronizer.class.getName());

    private static final int SYNC_DELAY_MS = 5 * 60 * 1000;

    private FormStoreImpl formStore;
    private ActivityInfoClientAsync client;

    private final Observable<Optional<SnapshotDelta>> snapshot;

    private final Subscription subscription;
    private OfflineStore offlineStore;


    public RecordSynchronizer(HttpStore httpStore, OfflineStore offlineStore) {
        this.offlineStore = offlineStore;
        this.snapshot = SnapshotDelta.compute(httpStore, offlineStore.getOfflineForms(), offlineStore.getCurrentSnapshot());
        this.subscription = snapshot.subscribe(this::onSnapshotUpdated);
    }

    private void onSnapshotUpdated(Observable<Optional<SnapshotDelta>> snapshotObservable) {
        if(snapshotObservable.isLoaded()) {
            snapshotObservable.get().ifPresent(snapshot -> {
                LOGGER.info("New snapshot loaded.");
                offlineStore.store(snapshot);
            });
        }
    }

    public void start() {
        com.google.gwt.core.client.Scheduler.get().scheduleFixedDelay(() -> {
            offlineStore.syncChanges();
            return true;
        }, SYNC_DELAY_MS);
    }
}
