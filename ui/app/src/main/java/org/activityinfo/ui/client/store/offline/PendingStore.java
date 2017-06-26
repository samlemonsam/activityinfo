package org.activityinfo.ui.client.store.offline;

import org.activityinfo.indexedb.IDBDatabaseUpgrade;
import org.activityinfo.indexedb.IDBObjectStore;
import org.activityinfo.indexedb.ObjectStoreDefinition;
import org.activityinfo.indexedb.ObjectStoreOptions;
import org.activityinfo.model.resource.RecordTransaction;
import org.activityinfo.store.query.shared.PendingSlot;

/**
 * IndexedDB store that records pending Record transactions offline
 */
public class PendingStore {

    public static final ObjectStoreDefinition<PendingStore> DEF = new ObjectStoreDefinition<PendingStore>() {
        @Override
        public String getName() {
            return "pending";
        }

        @Override
        public void upgrade(IDBDatabaseUpgrade database, int oldVersion) {
            if(oldVersion < 1) {
                database.createObjectStore(getName(), ObjectStoreOptions.withDefaults());
            }
        }

        @Override
        public PendingStore wrap(IDBObjectStore store) {
            return new PendingStore(store);
        }
    };
    private IDBObjectStore<PendingTransaction> store;

    public PendingStore(IDBObjectStore<PendingTransaction> store) {
        this.store = store;
    }

    public void put(PendingTransaction pendingTransaction) {
        this.store.put(pendingTransaction.getId(), pendingTransaction);
    }
}
