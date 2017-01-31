package org.activityinfo.ui.client.component.form.field.hierarchy;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;

/**
 * 
 */
public class Choice {
    private RecordRef ref;
    private String label;
    private RecordRef parentRef;

    public Choice(ResourceId formId, ResourceId id, String label) {
        this.label = label;
        this.ref = new RecordRef(formId, id);
    }

    public Choice(ResourceId formId, ResourceId id, String label, RecordRef parentRef) {
        this.label = label;
        this.ref = new RecordRef(formId, id);
        this.parentRef = parentRef;
    }

    public RecordRef getRef() {
        return ref;
    }

    public String getLabel() {
        return label;
    }

    public RecordRef getParentRef() {
        assert parentRef != null;
        return parentRef;
    }

    public boolean hasParent() {
        return parentRef != null;
    }
}
