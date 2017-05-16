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
    private final ColumnView longitude;
    private final ColumnView latitude;

    public OptionSet(ResourceId formId, ColumnSet columnSet) {
        this.formId = formId;
        this.columnSet = columnSet;
        this.id = columnSet.getColumnView("id");
        this.label = columnSet.getColumnView("label");
        this.longitude = columnSet.getColumnView("longitude");
        this.latitude = columnSet.getColumnView("latitude");
    }

    public ResourceId getFormId() {
        return formId;
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

    public double getLatitude(int i) {
        assert latitude != null : "latitude was not included in query";
        return latitude.getDouble(i);
    }

    public double getLongitude(int i) {
        assert longitude != null : "longitude was not included in query";
        return longitude.getDouble(i);
    }
}
