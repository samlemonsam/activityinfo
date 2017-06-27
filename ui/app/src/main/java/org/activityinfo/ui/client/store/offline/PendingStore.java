package org.activityinfo.ui.client.store.offline;

import com.google.common.base.Function;
import org.activityinfo.indexedb.*;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
                database.createObjectStore(getName(), ObjectStoreOptions.withAutoIncrement());
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
        this.store.put(pendingTransaction);
    }

    /**
     * Queries the list of pending transactions for the first transaction "ready" to send.
     * If one is found, its status is updated to "pending" and it is returned.
     */
    public Promise<Optional<PendingEntry>> takeNextReady() {
        Promise<Optional<PendingEntry>> result = new Promise<>();
        this.store.openCursor(new IDBCursorCallback<PendingTransaction>() {
            @Override
            public void onNext(IDBCursor<PendingTransaction> cursor) {
                if(cursor.getValue().isReady()) {
                    // Mark the transaction as being sent
                    PendingTransaction tx = cursor.getValue();
                    tx.setStatus(PendingTransaction.PENDING);
                    cursor.update(tx);

                    result.onSuccess(Optional.of(new PendingEntry(cursor.getKeyNumber(), cursor.getValue())));

                } else {
                    cursor.continue_();
                }
            }

            @Override
            public void onDone() {
                result.onSuccess(Optional.empty());
            }
        });
        return result;
    }

    public Promise<Void> updateStatus(int id, String status) {
        return this.store.get(id).then(new Function<PendingTransaction, Void>() {
            @Override
            public Void apply(PendingTransaction pendingTransaction) {
                // If it's already been removed from the queue, no need to update
                if(pendingTransaction != null) {
                    pendingTransaction.setStatus(status);
                    store.put(id, pendingTransaction);
                }
                return null;
            }
        });
    }

    public Promise<Void> remove(int id) {
        this.store.delete(id);
        return Promise.done();
    }

    public Promise<PendingStatus> getStatus() {
        Promise<PendingStatus> result = new Promise<>();
        this.store.openCursor(new IDBCursorCallback<PendingTransaction>() {

            private int count = 0;
            private Set<ResourceId> forms = new HashSet<>();

            @Override
            public void onNext(IDBCursor<PendingTransaction> cursor) {
                count++;
                forms.addAll(cursor.getValue().getTransaction().getAffectedFormIds());
                cursor.continue_();
            }

            @Override
            public void onDone() {
                result.onSuccess(new PendingStatus(count, forms));
            }
        });
        return result;
    }
}
