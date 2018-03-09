/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.server.command.handler.binding.dim;

import com.google.common.base.Optional;
import org.activityinfo.legacy.shared.reports.content.AttributeCategory;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.model.AttributeGroupDimension;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.store.mysql.metadata.Activity;

import java.util.*;

public class AttributeDimBinding extends DimBinding {

    private final AttributeGroupDimension model;
    private final ResourceId groupId;
    private final Optional<FormField> groupField;
    private final Map<String, Integer> attributeOrder;
    private final String columnId;

    public AttributeDimBinding(AttributeGroupDimension model, Collection<FormTree> formTrees) {
        this.model = model;
        
        // The way that attributes are handled in pivot queries has evolved a bit strangely.
        // Here's how it's currently designed:
        // 1) The Attribute group id defines the dimension.
        // 2) OTHER attributes in other forms with the same name as the attribute group name are also
        //    included
        
        this.groupId = CuidAdapter.attributeGroupField(model.getAttributeGroupId());
        this.groupField = findGroupName(formTrees);
        this.attributeOrder = findAttributeOrder(groupField);
        this.columnId = "A" + model.getAttributeGroupId();
    }

    private Map<String, Integer> findAttributeOrder(Optional<FormField> field) {
        Map<String, Integer> map = new HashMap<>();
        if(field.isPresent() && field.get().getType() instanceof EnumType) {
            EnumType type = (EnumType) field.get().getType();
            List<EnumItem> items = type.getValues();
            for (int i = 0; i < items.size(); i++) {
                map.put(items.get(i).getLabel(), i);
            }
        }
        return map;
    }

    private Optional<FormField> findGroupName(Collection<FormTree> formTrees) {
        for (FormTree formTree : formTrees) {
            for (FormTree.Node node : formTree.getLeaves()) {
                if(node.getFieldId().equals(groupId)) {
                    return Optional.of(node.getField());
                }
            }
        }
        return Optional.absent();
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
            return Collections.singletonList(new ColumnModel().setFormula(fieldId).as(columnId));
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
            if (field.isEnum() && groupField.isPresent() && field.getField().getLabel().equals(groupField.get().getLabel())) {
                return field.getPath();
            }
        }
        return null;
    }


    @Override
    public DimensionCategory[] extractCategories(Activity activity, ColumnSet columnSet) {

        DimensionCategory c[] = new DimensionCategory[columnSet.getNumRows()];

        if (columnSet.getColumns().containsKey(columnId)) {
            ColumnView view = columnSet.getColumnView(columnId);

            for (int i = 0; i < columnSet.getNumRows(); i++) {
                String value = view.getString(i);
                if (value != null) {
                    c[i] = new AttributeCategory(value, getSortOrder(value));
                }
            }
        } 
        return c;
    }

    private int getSortOrder(String value) {
        Integer order = attributeOrder.get(value);
        if(order == null) {
            return Integer.MAX_VALUE;
        }
        return order;
    }

}
