package org.activityinfo.model.type;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Field to which a sequential number is assigned when the record is first saved.
 *
 * <p>The Serial number can include an optional "prefix" which is calculated from the other
 * fields when the record is first created.</p>
 *
 */
public class SerialNumberType implements ParametrizedFieldType {

    public static final FieldTypeClass TYPE_CLASS = new ParametrizedFieldTypeClass() {

        @Override
        public String getId() {
            return "serial";
        }

        @Override
        public FieldType createType() {
            return new SerialNumberType();
        }

        @Override
        public FieldType deserializeType(JsonObject parametersObject) {
            SerialNumberType type = new SerialNumberType();
            if(parametersObject.has("prefixFormula")) {
                type.prefixFormula = parametersObject.get("prefixFormula").getAsString();
            }
            if(parametersObject.has("digits")) {
                type.digits = parametersObject.get("digits").getAsInt();
            }
            return type;
        }
    };

    private String prefixFormula;
    private int digits = 5;

    public SerialNumberType() {
    }

    public SerialNumberType(String prefixFormula, int digits) {
        this.prefixFormula = prefixFormula;
        this.digits = digits;
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public FieldValue parseJsonValue(JsonElement jsonElement) {
        if(jsonElement.isJsonPrimitive()) {
            JsonPrimitive value = jsonElement.getAsJsonPrimitive();
            if (value.isNumber()) {
                return new SerialNumber(value.getAsInt());
            }
        } else if(jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return new SerialNumber(
                    jsonObject.get("prefix").getAsString(),
                    jsonObject.get("number").getAsInt());
        }

        throw new IllegalArgumentException();
    }

    /**
     * @return the formula used to compute the serial number prefix.
     */
    public String getPrefixFormula() {
        return prefixFormula;
    }

    public SerialNumberType withPrefixFormula(String formula) {
        return new SerialNumberType(formula, this.digits);
    }


    public boolean hasPrefix() {
        return !Strings.isNullOrEmpty(prefixFormula);
    }

    /**
     *
     * @return the number of digits to use when displaying the record number.
     */
    public int getDigits() {
        return digits;
    }

    @Override
    public <T> T accept(FieldTypeVisitor<T> visitor) {
        return visitor.visitSerialNumber(this);
    }

    @Override
    public boolean isUpdatable() {
        return false;
    }

    @Override
    public JsonObject getParametersAsJson() {
        JsonObject object = new JsonObject();
        if(!Strings.isNullOrEmpty(prefixFormula)) {
            object.addProperty("prefixFormula", prefixFormula);
        }
        object.addProperty("digits", digits);
        return object;
    }

    @Override
    public boolean isValid() {
        return true;
    }


    public String format(SerialNumber value) {
        String formatted = Strings.padStart(Integer.toString(value.getNumber()), digits, '0');
        if(value.hasPrefix()) {
            formatted = value.getPrefix() + "-" + formatted;
        }
        return formatted;
    }
}
