package org.activityinfo.model.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Field to which a sequential number is assigned when the record is first saved.
 *
 */
public class SerialNumberType implements FieldType {


    public static final FieldTypeClass TYPE_CLASS = new ParametrizedFieldTypeClass() {
        @Override
        public FieldType deserializeType(JsonObject parametersObject) {
            return new SerialNumberType();
        }

        @Override
        public String getId() {
            return "serial";
        }

        @Override
        public FieldType createType() {
            return new SerialNumberType();
        }
    };

    @Override
    public FieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public FieldValue parseJsonValue(JsonElement value) {
        return new SerialNumber(value.getAsNumber().intValue());
    }

    @Override
    public <T> T accept(FieldTypeVisitor<T> visitor) {
        return visitor.visitFileNumber(this);
    }

    @Override
    public boolean isUpdatable() {
        return false;
    }
}
