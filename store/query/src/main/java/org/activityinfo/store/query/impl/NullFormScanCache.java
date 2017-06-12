package org.activityinfo.store.query.impl;

import com.google.common.util.concurrent.Futures;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;


public class NullFormScanCache implements FormScanCache {
    @Override
    public Map<String, Object> getAll(Set<String> keys) {
        return Collections.emptyMap();
    }

    @Override
    public Future<Integer> enqueuePut(Map<String, Object> toPut) {
        return Futures.immediateFuture(0);
    }


}