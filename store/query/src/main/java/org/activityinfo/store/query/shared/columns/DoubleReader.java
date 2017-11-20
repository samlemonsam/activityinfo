package org.activityinfo.store.query.shared.columns;

import org.activityinfo.model.type.FieldValue;

public interface DoubleReader {

    double read(FieldValue value);
}
