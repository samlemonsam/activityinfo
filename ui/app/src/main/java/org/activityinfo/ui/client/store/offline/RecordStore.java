package org.activityinfo.ui.client.store.offline;

import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.promise.Promise;

import java.util.Optional;

/**
 * IndexedDB object store for individual form records.
 */
public class RecordStore {

    public static final String NAME = "records";
    private IDBObjectStore impl;

    RecordStore(IDBObjectStore impl) {
        this.impl = impl;
    }

    public final void put(FormRecord record) {
        impl.putJson(record.toJsonElement().toString());
    }

    public final Promise<Optional<FormRecord>> get(RecordRef ref) {
        return impl
        .getJson(key(ref))
        .then(json -> {
            if(json == null) {
                return Optional.empty();
            } else {
                return Optional.of(FormRecord.fromJson(json));
            }
        });
    }

    private String[] key(RecordRef ref) {
        return new String[] { ref.getFormId().asString(), ref.getRecordId().asString() };
    }
}
