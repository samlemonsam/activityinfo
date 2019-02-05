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
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.indexedb.OfflineDatabase;
import org.activityinfo.model.resource.TransactionMode;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.http.HttpStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateSynchronizer {

    private static final Logger LOGGER = Logger.getLogger(UpdateSynchronizer.class.getName());

    private OfflineDatabase database;
    private HttpStore httpStore;
    private EventBus eventBus;

    private StatefulValue<Boolean> running = new StatefulValue<>(false);

    private List<Promise<Void>> callbacks = new ArrayList<>();

    public UpdateSynchronizer(OfflineDatabase database, HttpStore httpStore, EventBus eventBus) {
        this.database = database;
        this.httpStore = httpStore;
        this.eventBus = eventBus;
    }

    public void start() {
        // Do not start if all ready running.
        if(isRunning().get()) {
            return;
        }
        isRunning().updateValue(true);
        next();
    }

    public StatefulValue<Boolean> isRunning() {
        return running;
    }

    private void next() {
        take().then(new AsyncCallback<Optional<PendingEntry>>() {
            @Override
            public void onFailure(Throwable caught) {
                LOGGER.log(Level.SEVERE, "Failed to retrieve next pending item.");
                onQueueFailed();
            }

            @Override
            public void onSuccess(Optional<PendingEntry> pendingTransaction) {
                if(pendingTransaction.isPresent()) {
                    sendUpdate(pendingTransaction.get());
                } else {
                    onQueueFinished();
                }
            }
        });
    }

    private Promise<Optional<PendingEntry>> take() {
        return database
            .begin(PendingStore.DEF)
            .readwrite()
            .query(transaction -> transaction.objectStore(PendingStore.DEF).takeNextReady());
    }

    private void onQueueFinished() {
        running.updateIfNotEqual(false);
    }

    private void onQueueFailed() {
        running.updateIfNotEqual(false);
    }

    private void sendUpdate(PendingEntry entry) {
        httpStore.updateRecords(entry.getTransaction().getTransaction(), TransactionMode.OFFLINE).then(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {

                LOGGER.info("Failed to send pending transaction #" + entry.getId() + " to server");

                database
                    .begin(PendingStore.DEF)
                    .readwrite()
                    .query(tx -> tx.objectStore(PendingStore.DEF).updateStatus(entry.getId(), PendingTransaction.READY))
                    .then(new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            LOGGER.log(Level.SEVERE,
                                "Failed to mark transaction #" + entry.getId() + " as ready", caught);
                            onQueueFailed();

                        }

                        @Override
                        public void onSuccess(Void aVoid) {
                            eventBus.fireEvent(new PendingStatusEvent());

                            onQueueFailed();
                        }
                    });
            }

            @Override
            public void onSuccess(Void aVoid) {
                LOGGER.info("Successfully sent pending transaction #" + entry.getId() + " to server");
                database
                    .begin(PendingStore.DEF)
                    .readwrite()
                    .query(tx -> tx.objectStore(PendingStore.DEF).remove(entry.getId()))
                    .then(new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            LOGGER.log(Level.SEVERE,
                                "Failed to remove transaction #" + entry.getId() + " from queue", caught);
                            onQueueFailed();

                        }

                        @Override
                        public void onSuccess(Void aVoid) {
                            eventBus.fireEvent(new PendingStatusEvent());

                            // Handle the next item in the queue
                            next();
                        }
                    });
            }
        });
    }
}
