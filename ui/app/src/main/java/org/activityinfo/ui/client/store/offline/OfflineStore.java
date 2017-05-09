package org.activityinfo.ui.client.store.offline;

import com.google.common.collect.ImmutableSet;
import org.activityinfo.api.client.FormRecordSet;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.ui.client.store.Snapshot;

import java.util.HashSet;
import java.util.Set;

/**
 * Interface to *something* that can store stuff offline.
 */
public class OfflineStore {


    private IDBExecutor executor;

    private StatefulValue<Set<ResourceId>> offlineForms = new StatefulValue<>(ImmutableSet.of());

    public OfflineStore(IDBExecutor executor) {
        this.executor = executor;
    }

    /**
     * Try to load a cached FormSchema from the offline store.
     */
    public Observable<FormMetadata> getCachedMetadata(ResourceId formId) {
        return new CachedMetdata(executor, formId);
    }

    /**
     * Updates whether a form should be available offline.
     */
    public void enableOffline(ResourceId formId, boolean offline) {
        Set<ResourceId> newSet = new HashSet<>(offlineForms.get());
        if(offline) {
            newSet.add(formId);
        } else {
            newSet.remove(formId);
        }
        offlineForms.updateIfNotEqual(ImmutableSet.copyOf(newSet));
    }


    public Observable<FormRecord> getCachedRecord(RecordRef recordRef) {
        return new CachedRecord(recordRef, executor);
    }


    /**
     * @return the set of forms that should be made available offline.
     */
    public Observable<Set<ResourceId>> getOfflineForms() {
        return offlineForms;
    }

    /**
     * Stores a new snapshot to the remote store
     */
    public void store(Snapshot snapshot) {
        executor.begin()
        .objectStore(SchemaStore.NAME)
        .objectStore(RecordStore.NAME)
        .readwrite()
        .execute(tx -> {
            SchemaStore schemaStore = tx.schemas();
            for (FormMetadata metadata : snapshot.getForms()) {
                schemaStore.put(metadata.getSchema());
            }
            RecordStore recordStore = tx.records();
            for (FormRecordSet formRecordSet : snapshot.getRecordSets()) {
                for (FormRecord record : formRecordSet.getRecords()) {
                    recordStore.put(record);
                }
            }
        });
    }
}
