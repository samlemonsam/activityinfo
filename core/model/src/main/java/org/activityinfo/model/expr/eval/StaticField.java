package org.activityinfo.model.expr.eval;

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.FieldValues;
import org.activityinfo.model.type.NullFieldValue;

public class StaticField implements FieldValueSource {

    private FormField field;

    public StaticField(FormField field) {
        this.field = field;
    }

    @Override
    public FieldValue getValue(Resource instance, EvalContext context) {
        FieldValue fieldValue = FieldValues.readFieldValueIfType(
                instance,
                field.getId().asString(),
                field.getType().getTypeClass());
        if (fieldValue != null) {
            return fieldValue;
//        } else if(field.getDefaultValue() != null) {
//            return field.getDefaultValue();
        } else {
            // we don't want to get NPE in ComparisonOperator
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
