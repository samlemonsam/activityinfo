package org.activityinfo.ui.client.store.offline;

import org.activityinfo.promise.Promise;

/**
 * Interface to an Indexed DB object store.
 */
public interface IDBObjectStore {

    void putJson(String json);

    Promise<String> getJson(String key);

    Promise<String> getJson(String[] keys);
}
