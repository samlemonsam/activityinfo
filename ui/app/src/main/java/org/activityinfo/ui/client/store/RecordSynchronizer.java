package org.activityinfo.ui.client.store;

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;
import org.activityinfo.ui.client.store.http.HttpBus;
import org.activityinfo.ui.client.store.offline.OfflineStore;
import org.activityinfo.ui.client.store.offline.Snapshot;

import java.util.logging.Logger;

/**
 * Periodically compares the local state with the server and updates the local store.
 */
public class RecordSynchronizer {

    private static final Logger LOGGER = Logger.getLogger(RecordSynchronizer.class.getName());

    private FormStoreImpl formStore;
    private ActivityInfoClientAsync client;

    private final Observable<Snapshot> snapshot;

    private final Subscription subscription;
    private OfflineStore offlineStore;


    public RecordSynchronizer(HttpBus httpBus, OfflineStore offlineStore) {
        this.offlineStore = offlineStore;
        this.snapshot = Snapshot.compute(offlineStore.getOfflineForms(), httpBus);
        this.subscription = snapshot.subscribe(this::onSnapshotUpdated);
    }

    private void onSnapshotUpdated(Observable<Snapshot> snapshotObservable) {
        if(snapshotObservable.isLoaded()) {
            LOGGER.info("New snapshot loaded.");
            offlineStore.store(snapshotObservable.get());
        }
    }
}
