package org.activityinfo.model.type.time;

import org.activityinfo.model.type.FieldValue;

public interface TemporalValue extends FieldValue {

    LocalDateInterval asInterval();
}
