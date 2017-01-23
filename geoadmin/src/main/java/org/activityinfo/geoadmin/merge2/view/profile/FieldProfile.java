package org.activityinfo.geoadmin.merge2.view.profile;

import com.google.common.base.Strings;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.geo.Extents;
import org.activityinfo.model.type.geo.GeoArea;

/**
 * Describes the contents of a field in the source or target form.
 */
public class FieldProfile {

    private FormTree.Node node;
    private ColumnView columnView;

    private ColumnView xmin;
    private ColumnView xmax;
    private ColumnView ymin;
    private ColumnView ymax;

    public FieldProfile(FormTree.Node node, ColumnView columnView) {
        this.node = node;
        this.columnView = columnView;
    }

    public FieldProfile(FormTree.Node node, ColumnView xmin, ColumnView xmax, ColumnView ymin, ColumnView ymax) {
        this.node = node;
        this.xmin = xmin;
        this.xmax = xmax;
        this.ymin = ymin;
        this.ymax = ymax;
    }

    public boolean isText() {
        if(columnView == null) {
            return false;
        }
        return columnView.getType() == ColumnType.STRING;
    }
    
    public boolean isGeoArea() {
        if(columnView == null) {
            return false;
        }
        return node.getType() instanceof GeoArea;
    }


    public ResourceId getId() {
        return node.getFieldId();
    }

    public String getLabel() {
        if(node.isRoot()) {
            return node.getField().getLabel();
        } else {
            return node.getDefiningFormClass().getLabel() + "." + node.getField().getLabel();
        }
    }

    public ColumnView getView() {
        return columnView;
    }

    public Extents getExtents(int index) {
        return new Extents(ymin.getDouble(index),
                           ymax.getDouble(index),
                           xmin.getDouble(index),
                           xmax.getDouble(index));
    }

    public String getCode() {
        return node.getField().getCode();
    }

    public FieldPath getPath() {
        return node.getPath();
    }

    public FormTree.Node getNode() {
        return node;
    }
    
    public FormField getFormField() {
        return node.getField();
    }

    @Override
    public String toString() {
        return getLabel();
    }

    public boolean hasIdenticalContents(ColumnView view) {
        // Can only find text fields to be identical
        if(!this.isText() || view.getType() != ColumnType.STRING) {
            return false;
        }
    
        for (int i = 0; i < view.numRows(); i++) {
            String x = Strings.nullToEmpty(this.columnView.getString(i));
            String y = Strings.nullToEmpty(view.getString(i));
            if(!x.equalsIgnoreCase(y)) {
                return false;
            }
        }
        return true;
    }

}
