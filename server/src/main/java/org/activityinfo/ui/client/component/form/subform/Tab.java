package org.activityinfo.ui.client.component.form.subform;

import org.activityinfo.model.form.SubFormKind;

/**
 * A single tab, representing, for example 'May 2006'
 */
public class Tab {
    
    private String id;
    private String label;
    private SubFormKind kind;

    public Tab(String id, String label, SubFormKind kind) {
        this.id = id;
        this.label = label;
        this.kind = kind;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public SubFormKind getKind() {
        return kind;
    }
}
