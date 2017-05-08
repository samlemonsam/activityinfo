package org.activityinfo.ui.client.store;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.api.client.FormRecordSet;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.ui.client.store.offline.IDBTransaction;
import org.activityinfo.ui.client.store.offline.RecordStore;
import org.activityinfo.ui.client.store.offline.SchemaStore;
import org.activityinfo.ui.client.store.offline.VoidWork;

import java.util.Set;


public class IndexedDBOfflineStore implements OfflineStore {

    private StatefulValue<Set<ResourceId>> offlineForms = new StatefulValue<>();

    @Override
    public void putSchema(FormClass formSchema) {
        IndexedDB.begin(SchemaStore.NAME)
                .readwrite()
                .execute(new VoidWork() {
                    @Override
                    public void execute(IDBTransaction tx) {
                        tx.schemas().put(formSchema);
                    }
                });
    }

    @Override
    public void loadSchema(ResourceId formId, AsyncCallback<FormClass> callback) {
        IndexedDB.open().join(db -> db.loadSchema(formId)).then(callback);
    }

    @Override
    public void enableOffline(ResourceId formId, boolean offline) {

    }

    @Override
    public Observable<Set<ResourceId>> getOfflineForms() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void store(Snapshot snapshot) {
        IndexedDB.begin()
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
