package org.activityinfo.store.query.server;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.SerialNumberProvider;

import java.util.HashMap;
import java.util.Map;

public class SerialNumberProviderStub implements SerialNumberProvider {

    private Map<String, Integer> nextSerialNumber = new HashMap<>();

    @Override
    public int next(ResourceId formId, ResourceId fieldId, String prefix) {
        String key = formId.asString() + "-" + fieldId.asString() + "-" + prefix;
        Integer next = nextSerialNumber.get(key);
        if(next == null) {
            next = 1;
        }
        nextSerialNumber.put(key, next + 1);
        return next;
    }
}
