package org.activityinfo.ui.client.store;

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;
import org.activityinfo.ui.client.store.http.HttpBus;
import org.activityinfo.ui.client.store.offline.OfflineStore;

/**
 * Periodically compares the local state with the server and updates the local store.
 */
public class RecordSynchronizer {


    private FormStoreImpl formStore;
    private ActivityInfoClientAsync client;

    private final Observable<Snapshot> snapshot;

    private final Subscription subscription;
    private OfflineStore offlineStore;


    public RecordSynchronizer(HttpBus httpBus, OfflineStore offlineStore) {
        this.offlineStore = offlineStore;
        this.snapshot = Snapshot.get(offlineStore.getOfflineForms(), httpBus);
        this.subscription = snapshot.subscribe(this::onSnapshotUpdated);
    }

    private void onSnapshotUpdated(Observable<Snapshot> snapshotObservable) {
        if(snapshotObservable.isLoaded()) {
            offlineStore.store(snapshotObservable.get());
        }
    }

}
