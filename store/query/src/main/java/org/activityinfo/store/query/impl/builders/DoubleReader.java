package org.activityinfo.store.query.impl.builders;

import org.activityinfo.model.type.FieldValue;

public interface DoubleReader {

    double read(FieldValue value);
}
