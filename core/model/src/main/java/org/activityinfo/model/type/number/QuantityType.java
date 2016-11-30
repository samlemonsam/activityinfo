package org.activityinfo.model.type.number;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.JsonParsing;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceIdPrefixType;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.primitive.TextValue;

/**
 * A value types that describes a real-valued quantity and its units.
 */
public class QuantityType implements ParametrizedFieldType {


    public static class TypeClass implements ParametrizedFieldTypeClass, RecordFieldTypeClass {

        private TypeClass() {}

        @Override
        public String getId() {
            return "QUANTITY";
        }

        @Override
        public QuantityType createType() {
            return new QuantityType()
                    .setUnits(I18N.CONSTANTS.defaultQuantityUnits());
        }

        @Override
        public FieldType deserializeType(JsonObject parametersObject) {
            return new QuantityType(JsonParsing.toNullableString(parametersObject.get("units")));
        }

        @Override
        public FormClass getParameterFormClass() {
            FormClass formClass = new FormClass(ResourceIdPrefixType.TYPE.id("quantity"));
            formClass.addElement(new FormField(ResourceId.valueOf("units"))
                    .setType(FREE_TEXT.createType())
                    .setLabel("Units")
                    .setDescription("Describes the unit of measurement. For example: 'households', 'individuals'," +
                                    " 'meters', etc."));
            return formClass;
        }

    }

    public static final TypeClass TYPE_CLASS = new TypeClass();

    private String units;

    public QuantityType() {
    }

    public QuantityType(String units) {
        this.units = units;
    }

    public String getUnits() {
        return units;
    }

    public QuantityType setUnits(String units) {
        this.units = units;
        return this;
    }

    @Override
    public ParametrizedFieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public FieldValue parseJsonValue(JsonElement value) {
        if(value instanceof JsonNull) {
            return new Quantity(Double.NaN, units);
        } else {
            return new Quantity(value.getAsDouble(), units);
        }
    }

    @Override
    public FormInstance getParameters() {
        FormInstance instance = new FormInstance(null, getTypeClass().getParameterFormClass().getId());
        instance.set(ResourceId.valueOf("units"), TextValue.valueOf(units));
        return instance;
    }

    @Override
    public JsonObject getParametersAsJson() {
        JsonObject object = new JsonObject();
        object.addProperty("units", Strings.nullToEmpty(units));
        return object;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String toString() {
        return "QuantityType";
    }
}
