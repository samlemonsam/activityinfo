package org.activityinfo.store.mysql.metadata;

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.store.mysql.mapping.AttributeConverter;
import org.activityinfo.store.mysql.mapping.FieldValueConverter;
import org.activityinfo.store.mysql.mapping.QuantityConverter;
import org.activityinfo.store.mysql.mapping.TextConverter;

import java.io.Serializable;

public class ActivityField implements Serializable {
    
    int id;
    int sortOrder;
    int aggregation = 0;
    private final String category;
    private final FormField formField;

    public ActivityField(int id, String category, FormField formField, int sortOrder) {
        this.id = id;
        this.category = category;
        this.formField = formField;
        this.sortOrder = sortOrder;
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

    public FieldValueConverter getConverter() {
        if(isAttributeGroup()) {
            return new AttributeConverter();
        } else if(formField.getType() instanceof QuantityType) {
            QuantityType type = (QuantityType) formField.getType();
            return new QuantityConverter(type.getUnits());
        } else {
            return TextConverter.INSTANCE;
        }
    }

    public boolean isCalculated() {
        return formField.getType() instanceof CalculatedFieldType;
    }

    public ResourceId getResourceId() {
        return formField.getId();
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public int getAggregation() {
        return aggregation;
    }
}
