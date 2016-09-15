package org.activityinfo.ui.client.component.form.field;

import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;

/**
 * Wraps a column set with typed accessors.
 */
public class OptionSet {
    
    private ColumnSet columnSet;
    private ColumnView id;
    private ColumnView label;

    public OptionSet(ColumnSet columnSet) {
        this.columnSet = columnSet;
        this.id = columnSet.getColumnView("id");
        this.label = columnSet.getColumnView("label");
    }
    
    public int getCount() {
        return columnSet.getNumRows();
    }
    
    public String getId(int i) {
        return id.getString(i);
    }
    
    public String getLabel(int i) {
        return label.getString(i);
    }

    public ResourceId getRecordId(int i) {
        return ResourceId.valueOf(getId(i));
    }

    public ColumnView getColumnView(String name) {
        return columnSet.getColumnView(name);
    }
}
