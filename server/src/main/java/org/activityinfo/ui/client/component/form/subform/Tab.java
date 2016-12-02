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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tab tab = (Tab) o;

        return !(id != null ? !id.equals(tab.id) : tab.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
