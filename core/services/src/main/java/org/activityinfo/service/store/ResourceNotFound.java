package org.activityinfo.service.store;

import org.activityinfo.model.resource.ResourceId;

public class ResourceNotFound extends RuntimeException {

    private String resourceId;

    public ResourceNotFound() {
    }

    public ResourceNotFound(ResourceId resourceId) {
        super("Could not find resource [" + resourceId + "]");
        this.resourceId = resourceId.asString();
    }

    public String getResourceId() {
        return resourceId;
    }
}
