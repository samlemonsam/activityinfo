package org.activityinfo.ui.client.component.form.subform;

import org.activityinfo.model.type.FieldValue;

/**
 * A single period tab, representing, for example 'May 2006'
 */
public class PeriodTab {
    
    private String id;
    private String label;
    

    public PeriodTab(String id, String label) {
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
