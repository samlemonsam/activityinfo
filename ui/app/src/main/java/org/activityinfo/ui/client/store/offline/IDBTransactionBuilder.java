package org.activityinfo.ui.client.store.offline;


import org.activityinfo.promise.Promise;

/**
 * Convenience class to start an indexedDb transaction.
 */
public abstract class IDBTransactionBuilder {

    /**
     * Adds an object store to this transaction
     */
    public abstract IDBTransactionBuilder objectStore(String name);

    /**
     * Adds the given object stores to the transaction
     */
    public final IDBTransactionBuilder objectStores(String... names) {
        for (String name : names) {
            objectStore(name);
        }
        return this;
    }


    public abstract IDBTransactionBuilder readwrite();

    public final Promise<Void> execute(VoidWork work) {
        return query(transaction -> {
            work.execute(transaction);
            return Promise.done();
        });
    }

    public abstract <T> Promise<T> query(Work<T> work);
}
