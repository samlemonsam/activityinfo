package org.activityinfo.server.command.handler.binding;

import com.google.common.collect.Lists;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.BitSetColumnView;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;

import java.util.*;

public class AttributeFieldBinding implements FieldBinding<SiteDTO> {

    private FormField attrField;
    private EnumItem[] enumItems;

    public AttributeFieldBinding(FormField attrField) {
        this.attrField = attrField;
    }

    @Override
    public SiteDTO[] extractFieldData(SiteDTO[] dataArray, ColumnSet columnSet) {
        if (attrField == null || enumItems == null || enumItems.length < 1) {
            return dataArray;
        }

        ColumnView[] attrColumnViews = getAttributeColumnViews(columnSet);
        for (int i=0; i<attrColumnViews.length; i ++) {
            setAttributeValues(dataArray, enumItems[i], (BitSetColumnView) attrColumnViews[i]);
        }

        return dataArray;
    }

    private ColumnView[] getAttributeColumnViews(ColumnSet columnSet) {
        ColumnView[] attrColumnViews = new ColumnView[enumItems.length];

        for (int i=0; i<enumItems.length; i++) {
            attrColumnViews[i] = columnSet.getColumnView(enumItems[i].getId().asString());
        }

        return attrColumnViews;
    }

    protected void setAttributeValues(SiteDTO[] dataArray, EnumItem item, BitSetColumnView attrColumn) {
        int attrId = CuidAdapter.getLegacyIdFromCuid(item.getId());

        for (int i=0; i<attrColumn.numRows(); i++) {
            boolean selected = attrColumn.getBoolean(i) == BitSetColumnView.TRUE;
            dataArray[i].setAttributeValue(attrId, selected);
            if (selected) {
                dataArray[i].addDisplayAttribute(attrField.getId().asString(), item.getId().asString());
            }
        }
    }

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        if (!(attrField.getType() instanceof EnumType)) {
            return Lists.newArrayList();
        }
        EnumType type = (EnumType) attrField.getType();
        return createAttributeColumns(getEnumItemArray(type));
    }

    private EnumItem[] getEnumItemArray(EnumType type) {
        Object[] objArray = type.getValues().toArray();
        enumItems = new EnumItem[objArray.length];

        for (int i=0; i<objArray.length; i++) {
            enumItems[i] = (EnumItem) objArray[i];
        }

        return enumItems;
    }

    private List<ColumnModel> createAttributeColumns(EnumItem[] items) {
        List<ColumnModel> columns = new ArrayList<>(items.length);

        for (int i=0; i<items.length; i++) {
            String itemId = items[i].getId().asString();
            ExprNode expr = new CompoundExpr(attrField.getId(),itemId);
            columns.add(new ColumnModel().setExpression(expr).as(itemId));
        }

        return columns;
    }

    @Override
    public List<ColumnModel> getTargetColumnQuery(ResourceId targetFormId) {
        return null;
    }
}
