package org.activityinfo.server.command.handler.pivot;

import org.activityinfo.legacy.shared.reports.content.AttributeCategory;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.model.AttributeGroupDimension;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AttributeDimBinding extends DimBinding {

    private final AttributeGroupDimension model;
    private final ResourceId groupId;
    private final String groupName;
    private final String columnId;

    public AttributeDimBinding(AttributeGroupDimension model, Collection<FormTree> formTrees) {
        this.model = model;
        
        // The way that attributes are handled in pivot queries has evolved a bit strangely.
        // Here's how it's currently designed:
        // 1) The Attribute group id defines the dimension.
        // 2) OTHER attributes in other forms with the same name as the attribute group name are also
        //    included
        
        this.groupId = CuidAdapter.attributeGroupField(model.getAttributeGroupId());
        this.groupName = findGroupName(formTrees);
        this.columnId = "A" + model.getAttributeGroupId();
    }

    private String findGroupName(Collection<FormTree> formTrees) {
        for (FormTree formTree : formTrees) {
            for (FormTree.Node node : formTree.getLeaves()) {
                if(node.getFieldId().equals(groupId)) {
                    return node.getField().getLabel();
                }
            }
        }
        return null;
    }

    @Override
    public Dimension getModel() {
        return model;
    }

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {

        List<FormTree.Node> fields = formTree.getLeaves();

        // Does this form tree have the specific attribute group identified by the id?
        FieldPath fieldId = findFieldById(fields);
        if(fieldId == null) {
            fieldId = findFieldByName(fields);
        }
        
        if(fieldId != null) {
            return Collections.singletonList(new ColumnModel().setExpression(fieldId).as(columnId));
        } else {
            // this form has no corresponding attribute
            return Collections.emptyList();
        }
    }

    private FieldPath findFieldById(List<FormTree.Node> fields) {
        for (FormTree.Node field : fields) {
            if(field.getFieldId().equals(groupId)) {
                return field.getPath();
            }
        }
        return null;
    }


    private FieldPath findFieldByName(List<FormTree.Node> fields) {
        for (FormTree.Node field : fields) {
            if(field.isEnum() && field.getField().getLabel().equals(groupName)) {
                return field.getPath();
            }
        }
        return null;
    }


    @Override
    public DimensionCategory[] extractCategories(ActivityMetadata activity, FormTree formTree, ColumnSet columnSet) {

        DimensionCategory c[] = new DimensionCategory[columnSet.getNumRows()];

        if (columnSet.getColumns().containsKey(columnId)) {
            ColumnView view = columnSet.getColumnView(columnId);

            for (int i = 0; i < columnSet.getNumRows(); i++) {
                String value = view.getString(i);
                if (value != null) {
                    c[i] = new AttributeCategory(value, 0);
                }
            }
        } 
        return c;
    }
}
