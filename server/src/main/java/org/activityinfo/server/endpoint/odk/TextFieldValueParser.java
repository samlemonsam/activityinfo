package org.activityinfo.server.endpoint.odk;

import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;

class TextFieldValueParser implements FieldValueParser {
    @Override
    public FieldValue parse(String text) {
        return TextValue.valueOf(text);
    }
}
