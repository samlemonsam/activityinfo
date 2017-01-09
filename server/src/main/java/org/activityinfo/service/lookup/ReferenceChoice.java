package org.activityinfo.service.lookup;

import org.activityinfo.model.type.RecordRef;


public class ReferenceChoice {
    private final RecordRef ref;
    private String label;

    public ReferenceChoice(RecordRef ref, String label) {
        this.ref = ref;
        this.label = label;
    }

    public RecordRef getRef() {
        return ref;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
