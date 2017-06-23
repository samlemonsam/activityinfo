package org.activityinfo.store.mysql.metadata;

import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonParser;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.store.mysql.mapping.AttributeConverter;
import org.activityinfo.store.mysql.mapping.FieldValueConverter;
import org.activityinfo.store.mysql.mapping.QuantityConverter;
import org.activityinfo.store.mysql.mapping.TextConverter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ActivityField implements Serializable {
    
    int id;
    int sortOrder;
    int aggregation = 0;
    private final String category;
    private final FieldHolder formField = new FieldHolder();

    public ActivityField(int id, String category, FormField formField, int sortOrder) {
        this.id = id;
        this.category = category;
        this.formField.value = formField;
        this.sortOrder = sortOrder;
    }

    public int getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public FormField getFormField() {
        return formField.value;
    }

    public boolean isIndicator() {
        return !isAttributeGroup();
    }

    public boolean isAttributeGroup() {
        return formField.value.getType() instanceof EnumType;
    }
    
    public String getColumnName() {
        if(isAttributeGroup()) {
            return "attributeId";
        } else {
            if(formField.value.getType() instanceof QuantityType) {
                return "Value";
            } else {
                return "TextValue";
            }
        }
    }

    public FieldValueConverter getConverter() {
        if(isAttributeGroup()) {
            return new AttributeConverter();
        } else if(formField.value.getType() instanceof QuantityType) {
            QuantityType type = (QuantityType) formField.value.getType();
            return new QuantityConverter(type.getUnits());
        } else {
            return TextConverter.INSTANCE;
        }
    }

    public boolean isCalculated() {
        return formField.value.getType() instanceof CalculatedFieldType;
    }

    public ResourceId getResourceId() {
        return formField.value.getId();
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public int getAggregation() {
        return aggregation;
    }

    public static class FieldHolder implements Serializable {

        private FormField value;

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeUTF(value.toJsonObject().toString());
        }
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            this.value = FormField.fromJson((JsonObject)new JsonParser().parse(in.readUTF()));
        }
    }
}
