package org.activityinfo.ui.client.store.offline;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonValue;
import org.activityinfo.promise.Promise;

import java.util.*;


public class IDBExecutorStub implements IDBExecutor {

    private static final org.activityinfo.json.JsonParser JSON_PARSER = new org.activityinfo.json.JsonParser();

    private Map<String, ObjectStore> storeMap = new HashMap<>();

    public IDBExecutorStub() {
        storeMap.put(SchemaStore.NAME,
            new ObjectStore<>(JsonValue.class, SchemaStore.NAME, new String[]{"id"}));
        storeMap.put(RecordStore.NAME,
            new ObjectStore<>(RecordObject.class, RecordStore.NAME));
        storeMap.put(KeyValueStore.NAME,
            new ObjectStore<>(JsonValue.class, KeyValueStore.NAME));
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


    private class ObjectStore<T> {

        private final Class<T> valueType;
        private final String name;
        private final String[] keyPath;
        private TreeMap<ObjectKey, T> objectMap = new TreeMap<>();

        public ObjectStore(Class<T> valueType, String name, String[] keyPath) {
            this.valueType = valueType;
            this.name = name;
            this.keyPath = keyPath;
        }

        public ObjectStore(Class<T> valueType, String name) {
            this.valueType = valueType;
            this.name = name;
            this.keyPath = null;
        }

        public T get(ObjectKey key) {
            return objectMap.get(key);
        }
    }

    private class ObjectStoreInTx<T> implements IDBObjectStore<T> {

        private ObjectStore<T> store;
        private boolean readwrite;

        public ObjectStoreInTx(ObjectStore<T> store, boolean readwrite) {
            this.store = store;
            this.readwrite = readwrite;
        }
        @Override
        public void put(T value) {
            if(!readwrite) {
                throw new IllegalStateException("The transaction is read-only.");
            }
            store.objectMap.put(buildKey(Json.toJson(value).getAsJsonObject()), value);
        }

        @Override
        public void put(String key, T value) {
          put(new ObjectKey(key), value);
        }

        @Override
        public void put(String[] key, T object) {
            put(new ObjectKey(key), object);
        }

        private void put(ObjectKey key, T value) {
            if(!readwrite) {
                throw new IllegalStateException("The transaction is read-only.");
            }
            store.objectMap.put(key, value);
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
        public Promise<T> get(String key) {
            return get(new ObjectKey(key));
        }

        @Override
        public Promise<T> get(String[] keys) {
            return get(new ObjectKey(keys));
        }

        private Promise<T> get(ObjectKey key) {
            return Promise.resolved(store.get(key));
        }

        @Override
        public void openCursor(String[] lowerBound, String[] upperBound, IDBCursorCallback callback) {

            NavigableMap<ObjectKey, T> range = store.objectMap.subMap(
                    new ObjectKey(lowerBound), true,
                    new ObjectKey(upperBound), true);
            Iterator<Map.Entry<ObjectKey, T>> it = range.entrySet().iterator();

            Cursor<T> cursor = new Cursor<T>(it, callback);
            cursor.run();
        }
    }

}
