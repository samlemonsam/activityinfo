package org.activityinfo.server.endpoint.odk;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceValue;

class ReferenceFieldValueParser implements FieldValueParser {
    @Override
    public FieldValue parse(String text) {
        if (text == null) throw new IllegalArgumentException("Malformed Element passed to OdkFieldValueParser.parse()");

        return new ReferenceValue(ResourceId.valueOf(text));
    }
}
