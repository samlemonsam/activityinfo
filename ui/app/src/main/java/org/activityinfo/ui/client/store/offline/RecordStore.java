package org.activityinfo.ui.client.store.offline;

import org.activityinfo.model.form.FormRecord;

/**
 * IndexedDB object store for individual form records.
 */
public class RecordStore {

    public static final String NAME = "formRecords";
    private IDBObjectStore impl;

    protected RecordStore(IDBObjectStore impl) {
        this.impl = impl;
    }

    public final void put(FormRecord record) {
        impl.putJson(record.toJsonElement().toString());
    }

}
