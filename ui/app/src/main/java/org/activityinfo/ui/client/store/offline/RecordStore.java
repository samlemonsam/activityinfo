package org.activityinfo.ui.client.store.offline;

import org.activityinfo.model.form.FormRecord;

/**
 * IndexedDB object store for individual form records.
 */
public class RecordStore extends ObjectStore {

    public static final String NAME = "formRecords";

    protected RecordStore() {
    }

    public final void put(FormRecord record) {
        putJson(record.toJsonElement().toString());
    }

}
