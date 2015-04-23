package org.activityinfo.model.expr.eval;

import org.activityinfo.model.form.FormField;

public interface FieldValueSource extends ValueSource {

    FormField getField();
}
