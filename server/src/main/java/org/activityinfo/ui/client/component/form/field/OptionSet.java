package org.activityinfo.ui.client.component.form.field;

import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;

/**
 * Wraps a column set with typed accessors.
 */
public class OptionSet {

    private ResourceId formId;
    private ColumnSet columnSet;
    private ColumnView id;
    private ColumnView label;

    public OptionSet(ResourceId formId, ColumnSet columnSet) {
        this.formId = formId;
        this.columnSet = columnSet;
        this.id = columnSet.getColumnView("id");
        this.label = columnSet.getColumnView("label");
    }
    
    public int getCount() {
        return columnSet.getNumRows();
    }
    
    public RecordRef getRef(int i) {
        return new RecordRef(formId, ResourceId.valueOf(id.getString(i)));
    }
    
    public String getLabel(int i) {
        return label.getString(i);
    }

    public ColumnView getColumnView(String name) {
        return columnSet.getColumnView(name);
    }
}
