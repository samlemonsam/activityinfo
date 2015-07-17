package org.activityinfo.model.expr.eval;

import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;

public class ConstantValue implements ValueSource {

    private FieldValue value;

    public ConstantValue(FieldValue value) {
        this.value = value;
    }

    @Override
    public FieldValue getValue(Resource instance, EvalContext context) {
        return value;
    }

    @Override
    public FieldType resolveType(EvalContext context) {
        return value.getTypeClass().createType();
    }
}
