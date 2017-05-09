package org.activityinfo.ui.client.store.offline;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.activityinfo.promise.Promise;

import java.util.*;


public class IDBExecutorStub implements IDBExecutor {

    private static final JsonParser JSON_PARSER = new JsonParser();

    private Map<String, ObjectStore> storeMap = new HashMap<>();

    public IDBExecutorStub() {
        storeMap.put(SchemaStore.NAME, new ObjectStore(SchemaStore.NAME, new String[] { "id" }));
        storeMap.put(RecordStore.NAME, new ObjectStore(SchemaStore.NAME, new String[] { "formId", "recordId" }));

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
            Tx tx = new Tx(objectStores);
            return work.query(tx);
        }
    }

    private class Tx implements IDBTransaction {

        private Set<String> objectStores = new HashSet<>();

        public Tx(Set<String> objectStores) {
            this.objectStores = objectStores;
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
            return store;
        }
    }

    private class ObjectStore implements IDBObjectStore {


        private final String name;
        private final String[] keys;
        private Map<String, String> objectMap = new HashMap<>();

        public ObjectStore(String name, String[] keys) {
            this.name = name;
            this.keys = keys;
        }

        @Override
        public void putJson(String json) {
            JsonObject object = JSON_PARSER.parse(json).getAsJsonObject();
            String key = buildKey(object);
            objectMap.put(key, json);
        }

        private String buildKey(JsonObject object) {
            StringBuilder keyString = new StringBuilder();
            for (String  key : keys) {
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
            String object = objectMap.get(keyString.toString());
            if(object != null) {
                return Promise.resolved(object);
            } else {
                return Promise.rejected(new IllegalArgumentException("No such element: " + Arrays.toString(keys)));
            }
        }
    }

}
