package org.activityinfo.legacy.shared.model;

import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.command.result.ResourceResult;
import org.activityinfo.model.resource.ResourceId;

/**
 * Fetches a resource by Id;
 */
public class GetResource implements Command<ResourceResult> {

    private String id;

    public GetResource() {
    }

    public GetResource(ResourceId id) {
        this.id = id.asString();
    }

    public GetResource(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(ResourceId id) {
        this.id = id.asString();
    }
}
