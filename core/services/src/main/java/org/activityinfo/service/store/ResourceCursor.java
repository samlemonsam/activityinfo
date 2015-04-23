package org.activityinfo.service.store;

import org.activityinfo.model.resource.Resource;

public interface ResourceCursor {

    boolean next();

    Resource getResource();

}
