package org.activityinfo.geoadmin.merge2.view.profile;

import com.google.common.base.Strings;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Describes the contents of a field in the source or target form.
 */
public class FieldProfile {

    private FormTree.Node node;
    private ColumnView columnView;

    public FieldProfile(FormTree.Node node, ColumnView columnView) {
        this.node = node;
        this.columnView = columnView;
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
        return columnView.getType() == ColumnType.GEOGRAPHIC;
    }

    public Set<String> uniqueValues() {
        if(columnView == null || columnView.getType() != ColumnType.STRING) {
            return Collections.emptySet();
        } 
        
        Set<String> set = new HashSet<>();
        for(int i=0;i<columnView.numRows();++i) {
            String value = columnView.getString(i);
            if(!Strings.isNullOrEmpty(value)) {
                set.add(value.toUpperCase());
            }
        }
        return set;
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
