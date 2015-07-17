package org.activityinfo.server.endpoint.odk;

import org.activityinfo.model.type.FieldValue;

public interface FieldValueParser {
    public FieldValue parse(String string);
}
