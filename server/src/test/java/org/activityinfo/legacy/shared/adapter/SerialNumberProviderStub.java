package org.activityinfo.legacy.shared.adapter;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.SerialNumberProvider;

public class SerialNumberProviderStub implements SerialNumberProvider {
    private int next = 1;
    @Override
    public int next(ResourceId formId, ResourceId fieldId, String prefix) {
        return next++;
    }
}
