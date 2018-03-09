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
package org.activityinfo.indexedb;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IDBDatabaseStub implements IDBDatabase {

    private static final Logger LOGGER = Logger.getLogger(IDBDatabaseStub.class.getName());

    private String databaseName;
    private int version = 0;
    private Map<String, ObjectStoreStub> storeMap = new HashMap<>();

    private boolean open = true;

    public IDBDatabaseStub(String databaseName) {
        this.databaseName = databaseName;
    }

    public void maybeUpgrade(int openVersion, IDBOpenDatabaseCallback callback) {
        if (version < openVersion) {
            callback.onUpgradeNeeded(new InUpgrade(), version);
            version = openVersion;
        }
    }

    @Override
    public void transaction(String[] objectStores, String mode, IDBTransactionCallback callback) {

        // Verify that the object stores specified are present
        Set<String> missingStores = new HashSet<>();
        for (String objectStore : objectStores) {
            if(!storeMap.containsKey(objectStore)) {
                missingStores.add(objectStore);
            }
        }

        if(!missingStores.isEmpty()) {
            LOGGER.severe("Missing object stores: " + missingStores);
            callback.onError(null);
        }


        new Transaction(new HashSet<>(Arrays.asList(objectStores)), mode, callback).run();
    }

    @Override
    public void close() {
        open = false;
    }

    private class InUpgrade implements IDBDatabaseUpgrade {

        @Override
        public <T> IDBObjectStoreUpgrade createObjectStore(String name, ObjectStoreOptions options) {
            if(storeMap.containsKey(name)) {
                throw new IllegalStateException("ObjectStore '" + name + "' already exists.");
            }
            ObjectStoreStub<T> store = new ObjectStoreStub<T>(name, options);
            storeMap.put(name, store);

            return store.upgrade();
        }
    }

    private class Transaction implements IDBTransaction {

        private Set<String> transactionSet;
        private final String mode;
        private final IDBTransactionCallback callback;

        public Transaction(Set<String> transactionSet, String mode, IDBTransactionCallback callback) {
            this.transactionSet = transactionSet;
            this.mode = mode;
            this.callback = callback;
        }
        @Override
        public IDBObjectStore objectStore(String name) {
            if(!transactionSet.contains(name)) {
                throw new IllegalStateException("The object store '" + name + "' is not included in this transaction");
            }
            if(!storeMap.containsKey(name)) {
                throw new IllegalStateException("The object store '" + name + "' does not exist");
            }
            return storeMap.get(name).transaction(mode);
        }

        public void run() {
            boolean succeeded;
            try {
                callback.execute(this);
                succeeded = true;
            } catch (Exception e) {

                LOGGER.log(Level.SEVERE, "Transaction failed", e);

                succeeded = false;
                callback.onError(null);
            }
            if(succeeded) {
                callback.onComplete(null);
            }
        }
    }
}
