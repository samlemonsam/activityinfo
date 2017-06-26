package org.activityinfo.ui.client.store.offline;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.api.client.FormRecordSet;
import org.activityinfo.indexedb.IDBFactory;
import org.activityinfo.indexedb.OfflineDatabase;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.promise.Maybe;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.tasks.NullWatcher;
import org.activityinfo.ui.client.store.tasks.ObservableTask;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Interface to *something* that can store stuff offline.
 */
public class OfflineStore {

    private static final Logger LOGGER = Logger.getLogger(OfflineStore.class.getName());

    private OfflineDatabase executor;

    private StatefulValue<Set<ResourceId>> offlineForms = new StatefulValue<>();
    private StatefulValue<SnapshotStatus> currentSnapshot = new StatefulValue<>();

    public OfflineStore(IDBFactory indexedDbFactory) {
        this.executor = new OfflineDatabase(indexedDbFactory, "AI0001",
            SchemaStore.DEF, RecordStore.DEF, KeyValueStore.DEF);

        /*
         * Load current snapshot, if any present
         */
        this.executor.begin(KeyValueStore.DEF)
                .query(tx -> tx.objectStore(KeyValueStore.DEF).getCurrentSnapshot())
                .then(new AsyncCallback<SnapshotStatus>() {
                    @Override
                    public void onFailure(Throwable caught) {

                        LOGGER.log(Level.SEVERE, "Failed to load initial current snapshot", caught);

                        currentSnapshot.updateIfNotEqual(SnapshotStatus.EMPTY);
                    }

                    @Override
                    public void onSuccess(SnapshotStatus result) {

                        LOGGER.info("Loaded initial snapshot status: " + result);

                        currentSnapshot.updateIfNotEqual(result);
                    }
                });

        /*
         * Load current set of offline enabled forms
         */
        this.executor.begin(KeyValueStore.DEF)
                .query(tx -> tx.objectStore(KeyValueStore.DEF).getOfflineForms())
                .then(new AsyncCallback<Set<ResourceId>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        offlineForms.updateIfNotEqual(ImmutableSet.of());
                    }

                    @Override
                    public void onSuccess(Set<ResourceId> result) {
                        offlineForms.updateIfNotEqual(ImmutableSet.copyOf(result));
                    }
                });
    }

    /**
     * Try to load a cached FormSchema from the offline store.
     */
    public Observable<FormMetadata> getCachedMetadata(ResourceId formId) {
        return new ObservableTask<>(new MetadataQuery(executor, formId), NullWatcher.INSTANCE);
    }

    public Observable<Maybe<FormRecord>> getCachedRecord(RecordRef recordRef) {
        return new ObservableTask<>(new RecordQuery(executor, recordRef), NullWatcher.INSTANCE);
    }

    public Observable<ColumnSet> query(FormTree formTree, QueryModel queryModel) {
        return new ObservableTask<>(new ColumnQuery(executor, formTree, queryModel), NullWatcher.INSTANCE);
    }

    /**
     * Updates whether a form should be available offline.
     */
    public void enableOffline(ResourceId formId, boolean offline) {
        executor.begin(KeyValueStore.DEF)
        .readwrite()
        .query(tx -> {
            return tx.objectStore(KeyValueStore.DEF).getOfflineForms().then(new Function<Set<ResourceId>, Set<ResourceId>>() {
                @Override
                public Set<ResourceId> apply(Set<ResourceId> current) {
                    Set<ResourceId> updated = new HashSet<>(current);
                    if (offline) {
                        updated.add(formId);
                    } else {
                        updated.remove(formId);
                    }
                    tx.objectStore(KeyValueStore.DEF).put(updated);
                    return updated;
                }
            });
        }).then(new Function<Set<ResourceId>, Void>() {
            @Override
            public Void apply(Set<ResourceId> updated) {
                offlineForms.updateIfNotEqual(ImmutableSet.copyOf(updated));
                return null;
            }
        });
    }

    /**
     * @return the set of forms that should be made available offline.
     */
    public Observable<Set<ResourceId>> getOfflineForms() {
        return offlineForms;
    }

    public Observable<SnapshotStatus> getCurrentSnapshot() {
        return currentSnapshot;
    }


    /**
     * Stores a new snapshot to the remote store
     */
    public Promise<Void> store(Snapshot snapshot) {

        final SnapshotStatus status = new SnapshotStatus(snapshot);

        LOGGER.info("Updating offline snapshot: " + status);

        return executor.begin()
        .objectStore(SchemaStore.DEF)
        .objectStore(RecordStore.DEF)
        .objectStore(KeyValueStore.DEF)
        .readwrite()
        .execute(tx -> {
            SchemaStore schemaStore = tx.objectStore(SchemaStore.DEF);
            for (FormMetadata metadata : snapshot.getForms()) {
                schemaStore.put(metadata.getSchema());
            }
            RecordStore recordStore = tx.objectStore(RecordStore.DEF);
            for (FormRecordSet formRecordSet : snapshot.getRecordSets()) {
                for (FormRecord record : formRecordSet.getRecords()) {
                    recordStore.put(record);
                }
            }
            // Store our current status for future sessions
            tx.objectStore(KeyValueStore.DEF).put(status);
        })
        .then(new Function<Void, Void>() {
            @Override
            public Void apply(Void aVoid) {
                // Update listeners...
                currentSnapshot.updateIfNotEqual(new SnapshotStatus(snapshot));
                return null;
            }
        });
    }
}
