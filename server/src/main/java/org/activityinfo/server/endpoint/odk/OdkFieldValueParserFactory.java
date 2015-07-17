package org.activityinfo.server.endpoint.odk;

import org.activityinfo.model.type.FieldType;

public class OdkFieldValueParserFactory {
    static public OdkFieldValueParser fromFieldType(FieldType fieldType, boolean legacy) {
        return new OdkFieldValueParser(FieldValueParserFactory.fromFieldType(fieldType, true, legacy));
    }
}
