package org.activityinfo.store.query.impl;

import java.util.Map;
import java.util.Set;


public interface FormScanCache {

    /**
     * Retrieves all requested keys synchronously.
     */
    Map<String, Object> getAll(Set<String> keys);

    /**
     * Starts an asynchronous request to cache the given key/value pairs.
     */
    void enqueuePut(Map<String, Object> toPut);

    /**
     * Blocks until requests started by calls to {@link #enqueuePut(Map)} complete.
     */
    void waitUntilCached();
}
