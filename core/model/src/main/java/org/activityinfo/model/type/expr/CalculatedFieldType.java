package org.activityinfo.model.type.expr;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceIdPrefixType;
import org.activityinfo.model.type.*;

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
        public FieldType deserializeType(Record parameters) {
            CalculatedFieldType type = new CalculatedFieldType();
            Record expr = parameters.isRecord("expression");
            if(expr != null) {
                type.setExpression(ExprValue.fromRecord(expr));
            }
            return type;
        }

        @Override
        public FormClass getParameterFormClass() {

            FormField exprField = new FormField(ResourceId.valueOf("expression"));
            exprField.setLabel("Expression");
            exprField.setDescription("Example: A+B+(C/D)+[Volume A]");
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

    public ExprValue getExpression() {
        return expression;
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
    public Record getParameters() {
        Record record = new Record();
        record.set("classId", getTypeClass().getParameterFormClass().getId());
        if(expression != null) {
            record.set("expression", expression.asRecord());
        }
        return record;
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
