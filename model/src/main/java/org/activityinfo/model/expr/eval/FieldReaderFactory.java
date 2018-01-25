package org.activityinfo.model.expr.eval;

import org.activityinfo.model.form.FormField;

/**
 * Creates a FieldReader for a FormInstance type {@code InstanceT}
 */
public interface FieldReaderFactory<InstanceT> {

    FieldReader<InstanceT> create(FormField field);

}
