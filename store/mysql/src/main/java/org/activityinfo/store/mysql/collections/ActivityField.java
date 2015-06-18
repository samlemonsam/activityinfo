package org.activityinfo.store.mysql.collections;

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.store.mysql.mapping.FieldValueMapping;
import org.activityinfo.store.mysql.mapping.ForeignKeyMapping;
import org.activityinfo.store.mysql.mapping.Mapping;
import org.activityinfo.store.mysql.mapping.QuantityMapping;

public class ActivityField {
    
    int id;
    private final String category;
    private final FormField formField;

    public ActivityField(int id, String category, FormField formField) {
        this.id = id;
        this.category = category;
        this.formField = formField;
    }

    public int getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public FormField getFormField() {
        return formField;
    }

    public boolean isIndicator() {
        return !isAttributeGroup();
    }

    public boolean isAttributeGroup() {
        return formField.getType() instanceof EnumType;
    }
    
    public String getColumnName() {
        if(isAttributeGroup()) {
            return "attributeId";
        } else {
            if(formField.getType() instanceof QuantityType) {
                return "Value";
            } else {
                return "TextValue";
            }
        }
    }

    public FieldValueMapping getExtractor() {
        if(isAttributeGroup()) {
            return new ForeignKeyMapping(CuidAdapter.ATTRIBUTE_DOMAIN);
        } else if(formField.getType() instanceof QuantityType) {
            QuantityType type = (QuantityType) formField.getType();
            return new QuantityMapping(type.getUnits());
        } else {
            return Mapping.TEXT;
        }
    }

    public boolean isCalculated() {
        return formField.getType() instanceof CalculatedFieldType;
    }

    public ResourceId getResourceId() {
        return formField.getId();
    }

    //
//    public Join getJoin() {
//
//        String tableId = "I" + resourceId;
//        Join join = new Join(periodJoin, tableId,
//                ("LEFT JOIN indicatorvalue {table} ON (period.reportingPeriodId={table}.reportingPeriodId AND " +
//                        "{table}.indicatorId={indicatorId})")
//                        .replace("{table}", tableId)
//                        .replace("{indicatorId}", Integer.toString(id)));
//
//
//    }
}
