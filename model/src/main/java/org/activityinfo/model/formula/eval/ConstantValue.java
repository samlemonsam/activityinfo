package org.activityinfo.model.formula.eval;

import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;

public class ConstantValue implements ValueSource {

    private FieldValue value;

    public ConstantValue(FieldValue value) {
        this.value = value;
    }

    @Override
    public FieldValue getValue(FormInstance instance, EvalContext context) {
        return value;
    }

    @Override
    public FieldType resolveType(EvalContext context) {
        return value.getTypeClass().createType();
    }
}
