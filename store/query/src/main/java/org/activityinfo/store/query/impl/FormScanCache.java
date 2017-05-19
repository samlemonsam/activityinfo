package org.activityinfo.store.query.impl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;


public interface FormScanCache {

    /**
     * Retrieves all requested keys synchronously.
     */
    Map<String, Object> getAll(Set<String> keys);

    /**
     * Starts an asynchronous request to cache the given key/value pairs.
     * @return a Future containing the number of columns cached.
     */
    Future<Integer> enqueuePut(Map<String, Object> toPut);

}
