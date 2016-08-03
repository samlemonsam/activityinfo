package org.activityinfo.ui.client.component.form.subform;

/**
 * A single tab, representing, for example 'May 2006'
 */
public class Tab {
    
    private String id;
    private String label;

    public Tab(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

}
