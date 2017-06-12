package org.activityinfo.ui.client.store.offline;

/**
 * Interface to the IndexedDb Factory implementation
 */
public interface IDBExecutor {

    IDBTransactionBuilder begin(String... objectStores);

}
