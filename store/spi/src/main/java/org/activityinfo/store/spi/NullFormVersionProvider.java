package org.activityinfo.store.spi;

import org.activityinfo.model.resource.ResourceId;

public enum  NullFormVersionProvider implements FormVersionProvider {

    INSTANCE;

    @Override
    public long getCurrentFormVersion(ResourceId formId) {
        return 0;
    }
}
