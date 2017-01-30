package org.activityinfo.store.query.impl;

import java.util.Collections;
import java.util.Map;
import java.util.Set;


public class NullFormScanCache implements FormScanCache {
    @Override
    public Map<String, Object> getAll(Set<String> keys) {
        return Collections.emptyMap();
    }

    @Override
    public void enqueuePut(Map<String, Object> toPut) {

    }

    @Override
    public void waitUntilCached() {

    }


}