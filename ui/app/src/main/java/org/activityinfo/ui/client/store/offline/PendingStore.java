/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.store.offline;

import com.google.common.base.Function;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsDate;
import org.activityinfo.indexedb.*;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * IndexedDB store that records pending Record transactions offline
 */
public class PendingStore {

    /**
     * Limit how long a transaction can be checked out for synchronization.
     */
    private static final double PENDING_TIME_LIMIT_MS = 5d * 60d * 1000d;

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

                PendingTransaction tx = cursor.getValue();

                if(tx.isReady()) {
                    // Mark the transaction as being in transmission
                    tx.setStatus(PendingTransaction.PENDING);
                    tx.setCheckoutTime(currentTimeMillis());
                    cursor.update(tx);

                    result.onSuccess(Optional.of(new PendingEntry(cursor.getKeyNumber(), cursor.getValue())));
                    return;

                }

                if(PendingTransaction.PENDING.equals(tx.getStatus())) {

                    // Guard against pending transactions that are stuck in the queue.
                    // This can be caused because the browser is closed before the HTTP transaction completes
                    // and the status of the transaction is reset to READY, or because there was an error
                    // updating the status from Pending -> Ready after a failed sync.

                    double millisecondsSinceCheckout = currentTimeMillis() - tx.getCheckoutTime();

                    if(Double.isNaN(millisecondsSinceCheckout)) {
                        // This transaction was created by a client version before we added the checkout time.
                        // Set the checkout time to now and continue
                        tx.setCheckoutTime(currentTimeMillis());
                        cursor.update(tx);

                    } else if(millisecondsSinceCheckout > PENDING_TIME_LIMIT_MS) {
                        // Reset the checkout time and
                        // return as the next transaction to transmit
                        tx.setCheckoutTime(currentTimeMillis());
                        cursor.update(tx);

                        result.onSuccess(Optional.of(new PendingEntry(cursor.getKeyNumber(), cursor.getValue())));
                        return;
                    }
                }


                cursor.continue_();
            }

            @Override
            public void onDone() {
                result.onSuccess(Optional.empty());
            }
        });
        return result;
    }

    private double currentTimeMillis() {
        if(GWT.isClient()) {
            return JsDate.now();
        } else {
            return System.currentTimeMillis();
        }
    }

    public Promise<Void> updateStatus(int id, String status) {
        return this.store.get(id).then(new Function<PendingTransaction, Void>() {
            @Override
            public Void apply(PendingTransaction pendingTransaction) {
                // If it's already been removed from the queue, no need to update
                if(pendingTransaction != null) {
                    pendingTransaction.setStatus(status);
                    store.put(pendingTransaction, id);
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
