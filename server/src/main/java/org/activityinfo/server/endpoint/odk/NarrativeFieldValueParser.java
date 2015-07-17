package org.activityinfo.server.endpoint.odk;

import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.NarrativeValue;

class NarrativeFieldValueParser implements FieldValueParser {
    @Override
    public FieldValue parse(String text) {
        return NarrativeValue.valueOf(text);
    }
}
