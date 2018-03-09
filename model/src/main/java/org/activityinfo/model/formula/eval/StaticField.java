package org.activityinfo.model.formula.eval;

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.NullFieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;

public class StaticField implements FieldValueSource {

    private FormField field;

    public StaticField(FormField field) {
        this.field = field;
    }

    @Override
    public FieldValue getValue(FormInstance instance, EvalContext context) {
        FieldValue fieldValue = instance.get(field.getId(), field.getType().getTypeClass());
        if (fieldValue != null) {
            return fieldValue;
        } else {
            // we don't want to get NPE in ComparisonOperator

            if (field.getType() instanceof QuantityType) {
                return new Quantity(0);
            }

            return NullFieldValue.INSTANCE;
        }
    }

    @Override
    public FieldType resolveType(EvalContext context) {
        return field.getType();
    }

    public FormField getField() {
        return field;
    }
}
