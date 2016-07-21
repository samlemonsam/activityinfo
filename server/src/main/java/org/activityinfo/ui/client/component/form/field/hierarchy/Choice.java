package org.activityinfo.ui.client.component.form.field.hierarchy;

import org.activityinfo.model.resource.ResourceId;

/**
 * 
 */
public class Choice {
    private ResourceId formId;
    private ResourceId id;
    private String label;
    private ResourceId parentId;

    public Choice(ResourceId formId, ResourceId id, String label) {
        this.formId = formId;
        this.id = id;
        this.label = label;
    }

    public Choice(ResourceId formId, ResourceId id, String label, ResourceId parentId) {
        this.formId = formId;
        this.id = id;
        this.label = label;
        this.parentId = parentId;
    }

    public ResourceId getRootClassId() {
        return formId;
    }

    public ResourceId getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public ResourceId getParentId() {
        return parentId;
    }

    public boolean hasParent() {
        return parentId != null;
    }
}
