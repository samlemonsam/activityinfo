package org.activityinfo.store.spi;

import org.activityinfo.model.resource.ResourceId;

public interface FormVersionProvider {

    long getCurrentFormVersion(ResourceId formId);
}
