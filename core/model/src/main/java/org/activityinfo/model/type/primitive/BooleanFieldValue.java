package org.activityinfo.model.type.primitive;

import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;

public enum BooleanFieldValue implements FieldValue {
    TRUE,
    FALSE;

    @Override
    public FieldTypeClass getTypeClass() {
        return BooleanType.TYPE_CLASS;
    }

    public boolean asBoolean() {
        return this == TRUE;
    }

    public static BooleanFieldValue valueOf(boolean value) {
        return value ? TRUE : FALSE;
    }
}
