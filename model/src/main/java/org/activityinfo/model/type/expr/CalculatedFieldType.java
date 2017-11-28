package org.activityinfo.model.type.expr;

import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.JsonParsing;
import org.activityinfo.model.type.*;

import javax.annotation.Nullable;

import static org.activityinfo.json.Json.createObject;

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
        public FieldType deserializeType(JsonValue parametersObject) {
            JsonValue exprElement = parametersObject.get("formula");
            if(exprElement == null) {
                exprElement = parametersObject.get("expression");
            }
            if(exprElement == null) {
                return new CalculatedFieldType();
            } else if(exprElement.isJsonObject()) {
                JsonValue exprObject = exprElement;
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

    public CalculatedFieldType withFormula(String formula) {
        return new CalculatedFieldType(formula);
    }

    @Override
    public ParametrizedFieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public FieldValue parseJsonValue(JsonValue value) {
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
    public JsonValue getParametersAsJson() {
        JsonValue object = createObject();
        if (expression != null) {
            object.put("formula", expression);
        }
        return object;
    }

    @Override
    public boolean isValid() {
        return true;
    }

}
