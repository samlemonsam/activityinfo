package org.activityinfo.ui.client.store;

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;
import org.activityinfo.ui.client.store.http.HttpBus;

/**
 * Periodically compares the local state with the server and updates the local store.
 */
public class RecordSynchronizer {


    private FormStoreImpl formStore;
    private ActivityInfoClientAsync client;

    private final Observable<Snapshot> snapshot;

    private final Subscription subscription;


    public RecordSynchronizer(FormStoreImpl formStore, HttpBus httpBus) {
        this.formStore = formStore;
        this.snapshot = Snapshot.get(formStore.getSyncSet(), httpBus);
        this.subscription = snapshot.subscribe(this::onSnapshotUpdated);
    }

    private void onSnapshotUpdated(Observable<Snapshot> snapshotObservable) {
        if(snapshotObservable.isLoaded()) {
            throw new UnsupportedOperationException();
        }
    }

}
