package org.activityinfo.model.formula.eval;

import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;

public interface ValueSource {

    FieldValue getValue(FormInstance instance, EvalContext context);

    FieldType resolveType(EvalContext context);

}
