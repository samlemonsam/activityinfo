package org.activityinfo.model.type.expr;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.model.form.JsonParsing;
import org.activityinfo.model.type.*;

import javax.annotation.Nullable;

/**
 * A Value Type that represents a value calculated from a symbolic expression,
 * such as "A + B"
 */
public class CalculatedFieldType implements ParametrizedFieldType {

    public static final ParametrizedFieldTypeClass TYPE_CLASS = new ParametrizedFieldTypeClass() {
        @Override
        public String getId() {
            return "calculated";
        }

        @Override
        public FieldType createType() {
            return new CalculatedFieldType();
        }

        @Override
        public FieldType deserializeType(JsonObject parametersObject) {
            JsonElement exprElement = parametersObject.get("formula");
            if(exprElement == null) {
                exprElement = parametersObject.get("expression");
            }
            if(exprElement == null) {
                return new CalculatedFieldType();
            } else if(exprElement.isJsonObject()) {
                JsonObject exprObject = exprElement.getAsJsonObject();
                return new CalculatedFieldType(JsonParsing.toNullableString(exprObject.get("value")));
            } else {
                return new CalculatedFieldType(JsonParsing.toNullableString(exprElement));
            }
        }
    };


    private String expression;

    public CalculatedFieldType() {
    }

    public CalculatedFieldType(String expression) {
        this.expression = expression;
    }

    @Nullable
    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public ParametrizedFieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public FieldValue parseJsonValue(JsonElement value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T accept(FieldTypeVisitor<T> visitor) {
        return visitor.visitCalculated(this);
    }

    @Override
    public boolean isUpdatable() {
        return false;
    }

    @Override
    public JsonObject getParametersAsJson() {
        JsonObject object = new JsonObject();
        if (expression != null) {
            object.addProperty("formula", expression);
        }
        return object;
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
