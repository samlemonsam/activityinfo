package org.activityinfo.ui.client.store.offline;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.activityinfo.promise.Promise;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class IDBExecutorStub implements IDBExecutor {

    private static final JsonParser JSON_PARSER = new JsonParser();

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
        private final String[] keys;
        private Map<String, String> objectMap = new HashMap<>();

        public ObjectStore(String name, String[] keys) {
            this.name = name;
            this.keys = keys;
        }

        public ObjectStore(String name) {
            this.name = name;
            this.keys = null;
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
        public void putJson(String json) {
            if(!readwrite) {
                throw new IllegalStateException("The transaction is read-only.");
            }
            JsonObject object = JSON_PARSER.parse(json).getAsJsonObject();
            String key = buildKey(object);
            store.objectMap.put(key, json);
        }

        @Override
        public void putJson(String json, String key) {
            if(!readwrite) {
                throw new IllegalStateException("The transaction is read-only.");
            }
            store.objectMap.put(key, json);
        }

        private String buildKey(JsonObject object) {
            if(store.keys == null) {
                throw new IllegalStateException("Object store " + store.name +
                        " has no key path defined, must use out-of-line key");
            }

            StringBuilder keyString = new StringBuilder();
            for (String  key : store.keys) {
                JsonElement keyPart = object.get(key);
                if(keyPart == null) {
                    throw new IllegalStateException("Missing key '" + key + "' for object " + object);
                }
                keyString.append(keyPart.getAsString());
            }
            return keyString.toString();
        }

        @Override
        public Promise<String> getJson(String key) {
            return getJson(new String[] { key });
        }

        @Override
        public Promise<String> getJson(String[] keys) {
            StringBuilder keyString = new StringBuilder();
            for (String key : keys) {
                keyString.append(key);
            }
            String object = store.objectMap.get(keyString.toString());
            return Promise.resolved(object);
        }
    }

}
