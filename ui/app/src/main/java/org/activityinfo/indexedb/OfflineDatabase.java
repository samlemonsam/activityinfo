package org.activityinfo.indexedb;

import org.activityinfo.promise.Promise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Interface to the IndexedDb Factory implementation
 */
public class OfflineDatabase {

    private final int CURRENT_VERSION = 1;

    private IDBFactory factory;
    private String database;

    private List<ObjectStoreDefinition> stores = new ArrayList<>();

    public OfflineDatabase(IDBFactory factory, String databaseName, ObjectStoreDefinition... stores) {
        this.factory = factory;
        this.database = databaseName;
        this.stores = Arrays.asList(stores);
    }

    public TransactionBuilder begin(String... objectStores) {
        TransactionBuilder tx = new TransactionBuilder();
        for (String objectStore : objectStores) {
            tx.objectStore(objectStore);
        }
        return tx;
    }

    public TransactionBuilder begin(ObjectStoreDefinition... objectStores) {
        TransactionBuilder tx = new TransactionBuilder();
        for (ObjectStoreDefinition objectStore : objectStores) {
            tx.objectStore(objectStore);
        }
        return tx;
    }

    public TransactionBuilder begin() {
        return new TransactionBuilder();
    }

    private void upgrade(IDBDatabaseUpgrade database, int oldVersion) {
        for (ObjectStoreDefinition store : stores) {
            store.upgrade(database, oldVersion);
        }
    }

    public class TransactionBuilder {

        private List<String> objectStores = new ArrayList<>();
        private String mode = IDBDatabase.READONLY;

        private TransactionBuilder() {
        }

        public TransactionBuilder readwrite() {
            this.mode = IDBDatabase.READWRITE;
            return this;
        }

        public TransactionBuilder objectStore(String objectStore) {
            objectStores.add(objectStore);
            return this;
        }


        public TransactionBuilder objectStore(ObjectStoreDefinition<?> objectStore) {
            objectStores.add(objectStore.getName());
            return this;
        }
        public <T> Promise<T> query(Work<T> work) {

            Promise<Void> tx = new Promise<Void>();
            Promise<T> queryResult = new Promise<T>();

            factory.open(database, CURRENT_VERSION, new IDBOpenDatabaseCallback() {
                @Override
                public void onUpgradeNeeded(IDBDatabaseUpgrade database, int oldVersion) {
                    upgrade(database, oldVersion);
                }

                @Override
                public void onSuccess(IDBDatabase database) {
                    database.transaction(objectStores.toArray(new String[0]), mode, new IDBTransactionCallback() {
                        @Override
                        public void execute(IDBTransaction transaction) {
                            work.query(transaction).then(queryResult);
                        }

                        @Override
                        public void onComplete(IDBTransactionEvent event) {
                            tx.onSuccess(null);
                        }

                        @Override
                        public void onError(IDBTransactionEvent event) {
                            tx.onFailure(new RuntimeException("Transaction failed"));
                        }

                        @Override
                        public void onAbort(IDBTransactionEvent event) {
                            tx.onFailure(new RuntimeException("Transaction aborted"));
                        }
                    });
                }

                @Override
                public void onError(IDBOpenDatabaseEvent event) {
                    tx.onFailure(new RuntimeException("Failed to open database"));
                }

            });
            return tx.join(done -> queryResult);
        }

        public Promise<Void> execute(VoidWork voidWork) {
            return query(new Work<Void>() {
                @Override
                public Promise<Void> query(IDBTransaction transaction) {
                    voidWork.execute(transaction);
                    return Promise.done();
                }
            });
        }

    }

}
