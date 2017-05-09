package org.activityinfo.ui.client.store.offline;

/**
 * Interface to an Indexed DB Transaction
 */
public interface IDBTransaction {

    IDBObjectStore objectStore(String name);

    public default SchemaStore schemas() {
        return new SchemaStore(objectStore(SchemaStore.NAME));
    }

    public default RecordStore records() {
        return new RecordStore(objectStore(RecordStore.NAME));
    }

}
