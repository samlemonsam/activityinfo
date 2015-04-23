package org.activityinfo.service.lookup;

import org.activityinfo.model.resource.ResourceId;


public class ReferenceChoice {
    private ResourceId id;
    private String label;

    public ReferenceChoice(ResourceId id, String label) {
        this.id = id;
        this.label = label;
    }

    public ResourceId getId() {
        return id;
    }

    public void setId(ResourceId id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
