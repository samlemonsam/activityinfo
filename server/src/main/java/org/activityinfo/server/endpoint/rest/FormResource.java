package org.activityinfo.server.endpoint.rest;


import org.activityinfo.model.resource.ResourceId;

public class FormResource {
    private final ResourceId resourceId;

    public FormResource(String resourceId) {
        this.resourceId = ResourceId.valueOf(resourceId);
    }
}
