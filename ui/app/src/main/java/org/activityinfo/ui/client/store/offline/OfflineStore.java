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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.indexedb.IDBFactory;
import org.activityinfo.indexedb.IDBTransaction;
import org.activityinfo.indexedb.OfflineDatabase;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.*;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.RecordTransaction;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.ObservableTree;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.promise.Function2;
import org.activityinfo.promise.Maybe;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.FormChange;
import org.activityinfo.ui.client.store.FormChangeEvent;
import org.activityinfo.ui.client.store.FormTreeLoader;
import org.activityinfo.ui.client.store.http.FormChangeWatcher;
import org.activityinfo.ui.client.store.http.HttpStore;
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

    private Observable<PendingStatus> pendingStatus;

    private UpdateSynchronizer updateSynchronizer;

    public OfflineStore(HttpStore httpStore, IDBFactory indexedDbFactory) {
        this.database = new OfflineDatabase(indexedDbFactory, "AI0003",
            FormMetadataStore.DEF,
            SchemaStore.DEF,
            RecordStore.DEF,
            KeyValueStore.DEF,
            PendingStore.DEF);

        this.updateSynchronizer = new UpdateSynchronizer(database, httpStore, eventBus);
        this.pendingStatus = new ObservableTask<>(
            new PendingStatusQuery(database),
            PendingStatusEvent.watchFor(eventBus));

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

    public Observable<OfflineStatus> getStatus() {
        return Observable.transform(pendingStatus, currentSnapshot, new Function2<PendingStatus, SnapshotStatus, OfflineStatus>() {
            @Override
            public OfflineStatus apply(PendingStatus pendingStatus, SnapshotStatus snapshot) {
                return new OfflineStatus(snapshot, pendingStatus);
            }
        });
    }

    public Observable<PendingStatus> getPendingStatus() {
        return pendingStatus;
    }

    /**
     * Try to load a cached FormSchema from the offline store.
     */
    public Observable<FormMetadata> getCachedMetadata(ResourceId formId) {
        Observable<FormClass> schema = new ObservableTask<>(new SchemaQuery(database, formId),
            new FormChangeWatcher(eventBus, change -> change.isFormChanged(formId)));
        Observable<FormMetadataObject> metadata = new ObservableTask<>(new MetadataQuery(database, formId),
            new FormChangeWatcher(eventBus, change -> change.isFormChanged(formId)));

        return Observable.transform(schema, metadata,
            (s, m) -> FormMetadata.of(m.getVersion(), s, m.getPermissions()));
    }

    public Observable<Maybe<FormRecord>> getCachedRecord(RecordRef recordRef) {
        return new ObservableTask<>(new RecordQuery(database, recordRef),
            new FormChangeWatcher(eventBus, change -> change.isRecordChanged(recordRef)));
    }

    public Observable<ColumnSet> query(QueryModel queryModel) {
        ResourceId rootFormId = queryModel.getRowSources().get(0).getRootFormId();
        Observable<FormTree> tree = new ObservableTree<>(new FormTreeLoader(rootFormId, this::getCachedMetadata),
            com.google.gwt.core.client.Scheduler.get());

        return tree.join(formTree1 -> query(formTree1, queryModel));
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


    public void syncChanges() {
        updateSynchronizer.start();
    }

    /**
     * Stores a new snapshot to the remote store
     */
    public Promise<Void> store(SnapshotDelta snapshot) {

        final SnapshotStatus status = new SnapshotStatus(snapshot);

        LOGGER.info("Updating offline snapshot: " + status);

        return database.begin()
        .objectStore(FormMetadataStore.DEF)
        .objectStore(SchemaStore.DEF)
        .objectStore(RecordStore.DEF)
        .objectStore(KeyValueStore.DEF)
        .readwrite()
        .execute(tx -> {
            SchemaStore schemaStore = tx.objectStore(SchemaStore.DEF);
            FormMetadataStore metadataStore = tx.objectStore(FormMetadataStore.DEF);

            for (FormMetadata metadata : snapshot.getForms()) {
                metadataStore.put(metadata);
                schemaStore.put(metadata.getSchema());
            }
            RecordStore recordStore = tx.objectStore(RecordStore.DEF);
            applyRemoteUpdates(snapshot, recordStore);
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

    private void applyRemoteUpdates(SnapshotDelta snapshot, RecordStore recordStore) {
        for (FormSyncSet syncSet : snapshot.getSyncSets()) {
            applyRemoteUpdates(recordStore, syncSet);
        }
    }

    private void applyRemoteUpdates(RecordStore recordStore, FormSyncSet syncSet) {
        ResourceId formId = ResourceId.valueOf(syncSet.getFormId());

        if(syncSet.isReset()) {
            recordStore.deleteAllRecords(formId);
        } else {
            for (String recordId : syncSet.getDeleted()) {
                recordStore.deleteRecord(new RecordRef(formId, ResourceId.valueOf(recordId)));
            }
        }

        for (UpdatedRecord record : syncSet.getUpdatedRecords()) {
            recordStore.put(formId, record);
        }
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
                rollbacks.add(applyLocalUpdate(tx, update));
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

                    // Changes have been applied and the update added to the pending queue --
                    // let everyone know!

                    eventBus.fireEvent(new FormChangeEvent(FormChange.from(transaction)));
                    eventBus.fireEvent(new PendingStatusEvent());

                    // Try to synchronize immediately so that changes don't pile up.
                    syncChanges();

                    return null;
                }
            });
    }


    private Promise<RecordUpdate> applyLocalUpdate(IDBTransaction tx, RecordUpdate update) {
        RecordStore recordStore = tx.objectStore(RecordStore.DEF);
        return recordStore.get(update.getRecordRef()).then(existingRecord -> {

            // Queue the update to the record store
            if(update.isDeleted()) {
                if(existingRecord.isPresent()) {
                    recordStore.deleteRecord(update.getRecordRef());
                }
            } else {
                recordStore.put(update.getRecordRef(), applyLocalUpdate(existingRecord, update));
            }

            // Return the inverse so that we can rollback this change locally if the update
            // is eventually rejected by the server.
            return inverse(existingRecord, update);
        });
    }


    private RecordObject applyLocalUpdate(Optional<RecordObject> existingRecord, RecordUpdate update) {
        RecordObject updatedRecord = new RecordObject();

        // Combine old and new field values
        if(existingRecord.isPresent()) {
            updatedRecord.setParentRecordId(existingRecord.get().getParentRecordId());

            JsonValue existingFields = existingRecord.get().getFields();
            for (String fieldName : existingFields.keys()) {
                updatedRecord.setField(fieldName, existingFields.<JsonValue>get(fieldName));
            }
        }
        for (String fieldName : update.getFields().keys()) {
            updatedRecord.setField(fieldName, update.getFields().<JsonValue>get(fieldName));
        }

        // Only update parent record if this is a new record
        if(!existingRecord.isPresent()) {
            updatedRecord.setParentRecordId(update.getParentRecordId());
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

    public Promise<Void> clear() {
        return database.delete().then(new Function<Void, Void>() {
            @Override
            public Void apply(Void aVoid) {

                LOGGER.info("Database deleted");

                offlineForms.updateValue(Collections.emptySet());

                LOGGER.info("Offline form set updated");

                currentSnapshot.updateIfNotEqual(SnapshotStatus.EMPTY);

                LOGGER.info("Updated snapshot");

                eventBus.fireEvent(new PendingStatusEvent());
                return null;
            }
        });
    }
}
