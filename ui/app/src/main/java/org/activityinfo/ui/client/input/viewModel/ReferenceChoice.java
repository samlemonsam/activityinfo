package org.activityinfo.ui.client.input.viewModel;

import org.activityinfo.model.type.RecordRef;

public class ReferenceChoice {
    private RecordRef ref;
    private String label;


    public ReferenceChoice(RecordRef id, String label) {
        this.ref = id;
        this.label = label;
    }

    public String getKey() {
        return ref.toQualifiedString();
    }

    public RecordRef getRef() {
        return ref;
    }

    public String getLabel() {
        return label;
    }
}
