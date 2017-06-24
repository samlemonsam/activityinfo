package org.activityinfo.ui.client.store.offline;

import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonValue;
import org.activityinfo.promise.Promise;

import java.util.*;


public class IDBExecutorStub implements IDBExecutor {

    private static final org.activityinfo.json.JsonParser JSON_PARSER = new org.activityinfo.json.JsonParser();

    private Map<String, ObjectStore> storeMap = new HashMap<>();

    public IDBExecutorStub() {
        storeMap.put(SchemaStore.NAME, new ObjectStore(SchemaStore.NAME, new String[] { "id" }));
        storeMap.put(RecordStore.NAME, new ObjectStore(RecordStore.NAME, new String[] { "formId", "recordId" }));
        storeMap.put(KeyValueStore.NAME, new ObjectStore(KeyValueStore.NAME));
    }

    @Override
    public IDBTransactionBuilder begin(String... objectStores) {
        return new Builder().objectStores(objectStores);
    }

    private class Builder extends IDBTransactionBuilder {

        private Set<String> objectStores = new HashSet<>();
        private boolean readwrite = false;


        @Override
        public IDBTransactionBuilder objectStore(String name) {
            objectStores.add(name);
            return this;
        }

        @Override
        public IDBTransactionBuilder readwrite() {
            readwrite = true;
            return this;
        }

        @Override
        public <T> Promise<T> query(Work<T> work) {
            Tx tx = new Tx(objectStores, readwrite);
            return work.query(tx);
        }
    }

    private class Tx implements IDBTransaction {

        private Set<String> objectStores = new HashSet<>();
        private boolean readwrite;

        public Tx(Set<String> objectStores, boolean readwrite) {
            this.objectStores = objectStores;
            this.readwrite = readwrite;
        }

        @Override
        public IDBObjectStore objectStore(String name) {
            if(!objectStores.contains(name)) {
                throw new IllegalStateException("Object store " + name + " not included in transaction");
            }
            ObjectStore store = storeMap.get(name);
            if(store == null) {
                throw new IllegalStateException("Object store " + name + " does not exist.");
            }
            return new ObjectStoreInTx(store, readwrite);
        }
    }


    private class ObjectStore {

        private final String name;
        private final String[] keyPath;
        private TreeMap<ObjectKey, JsonValue> objectMap = new TreeMap<>();

        public ObjectStore(String name, String[] keyPath) {
            this.name = name;
            this.keyPath = keyPath;
        }

        public ObjectStore(String name) {
            this.name = name;
            this.keyPath = null;
        }
    }

    private class ObjectStoreInTx implements IDBObjectStore {

        private ObjectStore store;
        private boolean readwrite;

        public ObjectStoreInTx(ObjectStore store, boolean readwrite) {
            this.store = store;
            this.readwrite = readwrite;
        }
        @Override
        public void put(JsonValue value) {
            if(!readwrite) {
                throw new IllegalStateException("The transaction is read-only.");
            }
            store.objectMap.put(buildKey(value.getAsJsonObject()), value);
        }

        @Override
        public void put(String key, JsonValue value) {
            if(!readwrite) {
                throw new IllegalStateException("The transaction is read-only.");
            }
            store.objectMap.put(new ObjectKey(key), value);
        }

        private ObjectKey buildKey(JsonObject object) {
            if(store.keyPath == null) {
                throw new IllegalStateException("Object store " + store.name +
                        " has no key path defined, must use out-of-line key");
            }

            String[] key = new String[store.keyPath.length];
            for (int i = 0; i < store.keyPath.length; i++) {
                JsonValue keyPart = object.get(store.keyPath[i]);
                if(keyPart == null) {
                    throw new IllegalStateException("Missing key '" + store.keyPath[i] + "' for object " + object);
                }
                key[i] = keyPart.asString();
            }

            if(key.length == 1) {
                return new ObjectKey(key[0]);
            } else {
                return new ObjectKey(key);
            }
        }

        @Override
        public Promise<JsonValue> get(String key) {
            return get(new ObjectKey(key));
        }

        @Override
        public Promise<JsonValue> get(String[] keys) {
            return get(new ObjectKey(keys));
        }

        private Promise<JsonValue> get(ObjectKey key) {
            return Promise.resolved(store.objectMap.get(key));
        }

        @Override
        public void openCursor(String[] lowerBound, String[] upperBound, IDBCursorCallback callback) {

            NavigableMap<ObjectKey, JsonValue> range = store.objectMap.subMap(
                    new ObjectKey(lowerBound), true,
                    new ObjectKey(upperBound), true);
            Iterator<Map.Entry<ObjectKey, JsonValue>> it = range.entrySet().iterator();

            Cursor cursor = new Cursor(it, callback);
            cursor.run();
        }
    }

}
