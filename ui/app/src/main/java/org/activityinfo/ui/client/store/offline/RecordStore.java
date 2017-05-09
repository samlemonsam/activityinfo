package org.activityinfo.ui.client.store.offline;

import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.promise.Promise;

/**
 * IndexedDB object store for individual form records.
 */
public class RecordStore {

    public static final String NAME = "records";
    private IDBObjectStore impl;

    protected RecordStore(IDBObjectStore impl) {
        this.impl = impl;
    }

    public final void put(FormRecord record) {
        impl.putJson(record.toJsonElement().toString());
    }

    public final Promise<FormRecord> get(RecordRef ref) {
        return impl
                .getJson(key(ref))
                .then(json -> FormRecord.fromJson(json));
    }

    private String[] key(RecordRef ref) {
        return new String[] { ref.getFormId().asString(), ref.getRecordId().asString() };
    }
}
