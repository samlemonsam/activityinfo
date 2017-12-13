package org.activityinfo.model.expr.eval;

import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;

public interface FieldReader<InstanceT> {

    FieldValue readField(InstanceT record);

    FieldType getType();

}
