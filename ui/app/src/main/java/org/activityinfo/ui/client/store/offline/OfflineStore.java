package org.activityinfo.ui.client.store.offline;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.api.client.FormRecordSet;
import org.activityinfo.indexedb.IDBFactory;
import org.activityinfo.indexedb.IDBTransaction;
import org.activityinfo.indexedb.OfflineDatabase;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.RecordTransaction;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.promise.Maybe;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.FormChange;
import org.activityinfo.ui.client.store.FormChangeEvent;
import org.activityinfo.ui.client.store.http.FormChangeWatcher;
import org.activityinfo.ui.client.store.tasks.NullWatcher;
import org.activityinfo.ui.client.store.tasks.ObservableTask;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Interface to *something* that can store stuff offline.
 */
public class OfflineStore {

    private static final Logger LOGGER = Logger.getLogger(OfflineStore.class.getName());

    private OfflineDatabase database;

    private final EventBus eventBus = new SimpleEventBus();

    private StatefulValue<Set<ResourceId>> offlineForms = new StatefulValue<>();
    private StatefulValue<SnapshotStatus> currentSnapshot = new StatefulValue<>();

    public OfflineStore(IDBFactory indexedDbFactory) {
        this.database = new OfflineDatabase(indexedDbFactory, "AI0001",
            SchemaStore.DEF,
            RecordStore.DEF,
            KeyValueStore.DEF,
            PendingStore.DEF);

        /*
         * Load current snapshot, if any present
         */
        this.database.begin(KeyValueStore.DEF)
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
        this.database.begin(KeyValueStore.DEF)
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

    @VisibleForTesting
    OfflineDatabase getDatabase() {
        return database;
    }

    /**
     * Try to load a cached FormSchema from the offline store.
     */
    public Observable<FormMetadata> getCachedMetadata(ResourceId formId) {
        return new ObservableTask<>(new MetadataQuery(database, formId),
            new FormChangeWatcher(eventBus, change -> change.isFormChanged(formId)));
    }

    public Observable<Maybe<FormRecord>> getCachedRecord(RecordRef recordRef) {
        return new ObservableTask<>(new RecordQuery(database, recordRef),
            new FormChangeWatcher(eventBus, change -> change.isRecordChanged(recordRef)));
    }

    public Observable<ColumnSet> query(FormTree formTree, QueryModel queryModel) {
        return new ObservableTask<>(new ColumnQuery(database, formTree, queryModel),
            new FormChangeWatcher(eventBus, change -> true));
    }

    /**
     * Updates whether a form should be available offline.
     */
    public void enableOffline(ResourceId formId, boolean offline) {
        database.begin(KeyValueStore.DEF)
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

        return database.begin()
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

    /**
     * Applies a record transaction to the local IndexedDB and queues the transaction for
     * sending to the server
     */
    public Promise<Void> execute(RecordTransaction transaction) {

        return database.begin()
        .objectStore(RecordStore.DEF)
        .objectStore(PendingStore.DEF)
        .readwrite()
        .query(tx -> {

            List<Promise<RecordUpdate>> rollbacks = new ArrayList<>();

            for (RecordUpdate update : transaction.getChanges()) {
                rollbacks.add(applyUpdate(tx, update));
            }

            return Promise.flatten(rollbacks).then(new Function<List<RecordUpdate>, Void>() {
                @Override
                public Void apply(List<RecordUpdate> rollbacks) {
                    PendingTransaction pending = PendingTransaction.create(transaction, rollbacks);

                    LOGGER.info("Adding pending transaction: " + Json.stringify(pending));

                    tx.objectStore(PendingStore.DEF).put(pending);
                    return null;
                }
            });
        }).then(new Function<Void, Void>() {
                @Override
                public Void apply(Void aVoid) {

                    eventBus.fireEvent(new FormChangeEvent(FormChange.from(transaction)));

                    return null;
                }
            });
    }


    private Promise<RecordUpdate> applyUpdate(IDBTransaction tx, RecordUpdate update) {
        RecordStore recordStore = tx.objectStore(RecordStore.DEF);
        return recordStore.get(update.getRecordRef()).then(existingRecord -> {

            // Queue the update to the record store
            if(update.isDeleted()) {
                if(existingRecord.isPresent()) {
                    recordStore.deleteRecord(update.getRecordRef());
                }
            } else {
                recordStore.put(update.getRecordRef(), applyUpdate(existingRecord, update));
            }

            // Return the inverse so that we can rollback this change locally if the update
            // is eventually rejected by the server.
            return inverse(existingRecord, update);
        });
    }


    private RecordObject applyUpdate(Optional<RecordObject> existingRecord, RecordUpdate update) {
        RecordObject updatedRecord = new RecordObject();

        // Combine old and new field values
        if(existingRecord.isPresent()) {
            updatedRecord.setParentFormId(existingRecord.get().getParentFormId());

            JsonObject existingFields = existingRecord.get().getFields();
            for (String fieldName : existingFields.keys()) {
                updatedRecord.setField(fieldName, existingFields.<JsonValue>get(fieldName));
            }
        }
        for (String fieldName : update.getFields().keys()) {
            updatedRecord.setField(fieldName, update.getFields().<JsonValue>get(fieldName));
        }

        // Only update parent record if this is a new record
        if(!existingRecord.isPresent()) {
            updatedRecord.setParentFormId(update.getParentRecordId());
        }
        return updatedRecord;
    }

    private RecordUpdate inverse(Optional<RecordObject> existingRecord, RecordUpdate update) {
        RecordUpdate inverse = new RecordUpdate();
        inverse.setFormId(update.getFormId());
        inverse.setRecordId(update.getRecordId());

        if(existingRecord.isPresent()) {
            if(update.isDeleted()) {
                // Restore the old values
                inverse.setFields(existingRecord.get().getFields());

            } else {
                // Remember only the changed the old values so we can roll them back
                for (String updatedField : update.getFields().keys()) {
                    inverse.setFieldValue(updatedField, existingRecord.get().getField(updatedField));
                }
            }
        } else {
            inverse.setDeleted(true);
        }
        return inverse;
    }
}
