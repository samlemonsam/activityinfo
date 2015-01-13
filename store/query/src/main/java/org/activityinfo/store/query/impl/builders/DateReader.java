package org.activityinfo.store.query.impl.builders;

import org.activityinfo.model.type.FieldValue;

import java.util.Date;

public interface DateReader {

    Date readDate(FieldValue value);
}
