package org.activityinfo.ui.client.store.offline;

import com.google.common.base.Function;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.indexedb.OfflineDatabase;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.http.HttpStore;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateSynchronizer {

    private static final Logger LOGGER = Logger.getLogger(UpdateSynchronizer.class.getName());

    private OfflineDatabase database;
    private HttpStore httpStore;
    private EventBus eventBus;

    private StatefulValue<Boolean> running = new StatefulValue<>(false);


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
        take().join(new Function<Optional<PendingEntry>, Promise<Void>>() {
            @Override
            public Promise<Void> apply(Optional<PendingEntry> pendingTransaction) {
                if(pendingTransaction.isPresent()) {
                    sendUpdate(pendingTransaction.get());
                } else {
                    onQueueFinished();
                }
                return null;
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
        httpStore.updateRecords(entry.getTransaction().getTransaction()).then(new AsyncCallback<Void>() {
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
