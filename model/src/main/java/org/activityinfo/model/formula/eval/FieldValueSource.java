package org.activityinfo.model.formula.eval;

import org.activityinfo.model.form.FormField;

public interface FieldValueSource extends ValueSource {

    FormField getField();
}
