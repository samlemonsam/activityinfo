package org.activityinfo.server.endpoint.odk;

import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;

class TextFieldValueParser implements FieldValueParser {
    @Override
    public FieldValue parse(String text) {
        if (text == null) throw new IllegalArgumentException("Malformed Element passed to OdkFieldValueParser.parse()");

        return TextValue.valueOf(text);
    }
}
