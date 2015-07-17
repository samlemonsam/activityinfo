package org.activityinfo.server.endpoint.odk;

import com.google.common.base.Strings;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.time.LocalDate;

class LocalDateFieldValueParser implements FieldValueParser {
    @Override
    public FieldValue parse(String text) {
        if (Strings.isNullOrEmpty(text)) {
            return null;
        } else {
            if (text.contains("T")) {
                text = text.substring(0, text.indexOf("T"));
            }
            return LocalDate.parse(text);
        }
    }
}
