package org.activityinfo.geoadmin.merge2.view.model;

import com.google.common.base.Strings;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Describes the contents of a field in the source or target form.
 * 
 */
public class FieldProfile {

    private FormTree.Node node;
    private ColumnView columnView;

    public FieldProfile(FormTree.Node node, ColumnView columnView) {
        this.node = node;
        this.columnView = columnView;
    }


    public Set<String> uniqueValues() {
        if(columnView == null) {
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
}
