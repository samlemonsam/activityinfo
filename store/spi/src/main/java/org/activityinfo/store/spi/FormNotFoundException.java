package org.activityinfo.store.spi;

import org.activityinfo.model.resource.ResourceId;

public class FormNotFoundException extends RuntimeException {

    private String resourceId;

    public FormNotFoundException() {
    }

    public FormNotFoundException(ResourceId resourceId) {
        super("Could not find resource [" + resourceId + "]");
        this.resourceId = resourceId.asString();
    }

    public String getResourceId() {
        return resourceId;
    }
}
