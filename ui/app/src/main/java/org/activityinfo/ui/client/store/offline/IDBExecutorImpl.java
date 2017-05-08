package org.activityinfo.ui.client.store.offline;

public class IDBExecutorImpl implements IDBExecutor {

    @Override
    public IDBTransactionBuilder begin(String... objectStores) {
        return IDBDatabaseImpl.begin(objectStores);
    }
}
