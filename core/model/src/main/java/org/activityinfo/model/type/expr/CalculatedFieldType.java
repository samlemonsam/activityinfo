package org.activityinfo.model.type.expr;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.JsonParsing;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceIdPrefixType;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ParametrizedFieldType;
import org.activityinfo.model.type.ParametrizedFieldTypeClass;

import javax.annotation.Nonnull;
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
            JsonElement exprElement = parametersObject.get("expression");
            if(exprElement.isJsonObject()) {
                JsonObject exprObject = exprElement.getAsJsonObject();
                return new CalculatedFieldType(JsonParsing.toNullableString(exprObject.get("value")));
            } else {
                return new CalculatedFieldType(JsonParsing.toNullableString(exprElement));
            }
        }

        @Override
        public FormClass getParameterFormClass() {

            FormField exprField = new FormField(ResourceId.valueOf("expression"));
            exprField.setLabel(I18N.CONSTANTS.expression());
            exprField.setDescription(I18N.CONSTANTS.expressionExample());
            exprField.setType(ExprFieldType.INSTANCE);

            FormClass formClass = new FormClass(ResourceIdPrefixType.TYPE.id(getId()));
            formClass.addElement(exprField);

            return formClass;
        }
    };


    private ExprValue expression;

    public CalculatedFieldType() {
    }

    public CalculatedFieldType(String expression) {
        this.expression = ExprValue.valueOf(expression);
    }

    public CalculatedFieldType(ExprValue expression) {
        this.expression = expression;
    }

    @Nullable
    public ExprValue getExpression() {
        return expression;
    }
    
    @Nonnull
    public String getExpressionAsString() {
        if(expression == null) {
            return "";
        } 
        if(expression.getExpression() == null) {
            return "";
        }
        return expression.getExpression();
    }

    public void setExpression(String expression) {
        this.expression = ExprValue.valueOf(expression);
    }

    private void setExpression(ExprValue exprValue) {
        this.expression = exprValue;
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
    public FormInstance getParameters() {
        FormInstance instance = new FormInstance(null, getTypeClass().getParameterFormClass().getId());
        instance.set(ResourceId.valueOf("expression"), expression);
        return instance;
    }

    @Override
    public JsonObject getParametersAsJson() {
        JsonObject object = new JsonObject();
        if (expression != null) {
            object.addProperty("expression", expression.getExpression());
        }
        return object;
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
