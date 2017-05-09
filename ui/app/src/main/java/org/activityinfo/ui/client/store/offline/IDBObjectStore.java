package org.activityinfo.ui.client.store.offline;

import org.activityinfo.promise.Promise;

/**
 * Interface to an Indexed DB object store.
 */
public interface IDBObjectStore {

    void putJson(String json);

    void putJson(String json, String key);

    /**
     * Retrieves the given object from the ObjectStore, encoded as a JSON string, or {@code null} if no
     * such object exists.
     */
    Promise<String> getJson(String key);

    /**
     * Retrieves the given object from the ObjectStore, encoded as a JSON string, or {@code null} if no
     * such object exists.
     */
    Promise<String> getJson(String[] keys);
}
