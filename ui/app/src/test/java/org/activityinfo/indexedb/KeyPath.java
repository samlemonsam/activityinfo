package org.activityinfo.indexedb;

import org.activityinfo.json.JsonObject;

public interface KeyPath {

    ObjectKey buildKey(JsonObject value);

    public static KeyPath from(ObjectStoreOptions options) {
        if(options.getKeyPath() instanceof String) {
            String keyName = (String) options.getKeyPath();
            return jsonObject -> new ObjectKey(jsonObject.getString(keyName));
        }

        if(options.getKeyPath() instanceof String[]) {
            String keyNames[] = (String[]) options.getKeyPath();
            return jsonObject -> {
                String[] key = new String[keyNames.length];
                for (int i = 0; i < key.length; i++) {
                    key[i] = jsonObject.getString(keyNames[i]);
                }
                return new ObjectKey(key);
            };
        }

        return jsonObject -> {
            throw new UnsupportedOperationException("No key path provided");
        };
    }
}
