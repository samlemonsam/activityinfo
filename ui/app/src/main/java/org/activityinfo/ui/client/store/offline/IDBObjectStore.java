package org.activityinfo.ui.client.store.offline;

import org.activityinfo.json.JsonValue;
import org.activityinfo.promise.Promise;

/**
 * Interface to an Indexed DB object store.
 */
public interface IDBObjectStore<T> {

    void put(T object);

    void put(String key, T object);

    void put(String[] key, T object);

    /**
     * Retrieves the given object from the ObjectStore, encoded as a JSON string, or {@code null} if no
     * such object exists.
     */
    Promise<T> get(String key);

    /**
     * Retrieves the given object from the ObjectStore, encoded as a JSON string, or {@code null} if no
     * such object exists.
     */
    Promise<T> get(String[] keys);


    /**
     * Opens a cursor over the objects in the store with keys in the range [lower, bound]
     *
     * See {@linkplain https://w3c.github.io/IndexedDB/#key-construct} for the way in which keys are ordered.
     *
     * @param lowerBound the lower bound of the key, inclusive
     * @param upperBound the upper bound of the key, inclusive
     * @param callback
     */
    void openCursor(String[] lowerBound, String[] upperBound, IDBCursorCallback<T> callback);

}
