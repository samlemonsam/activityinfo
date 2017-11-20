package org.activityinfo.ui.client.store.offline;

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;
import org.activityinfo.ui.client.store.FormStoreImpl;
import org.activityinfo.ui.client.store.http.HttpStore;

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


    public RecordSynchronizer(HttpStore httpStore, OfflineStore offlineStore) {
        this.offlineStore = offlineStore;
        this.snapshot = Snapshot.compute(offlineStore.getOfflineForms(), httpStore);
        this.subscription = snapshot.subscribe(this::onSnapshotUpdated);
    }

    private void onSnapshotUpdated(Observable<Snapshot> snapshotObservable) {
        if(snapshotObservable.isLoaded()) {
            LOGGER.info("New snapshot loaded.");
            offlineStore.store(snapshotObservable.get());
        }
    }
}
