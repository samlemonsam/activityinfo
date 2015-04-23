package org.activityinfo.server.endpoint.odk;

import org.activityinfo.model.type.FieldValue;
import org.w3c.dom.Element;

final public class OdkFieldValueParser {
    final private FieldValueParser fieldValueParser;

    public OdkFieldValueParser(FieldValueParser fieldValueParser) {
        this.fieldValueParser = fieldValueParser;
    }

    public FieldValue parse(Element element) {
        return fieldValueParser.parse(OdkHelper.extractText(element));
    }
}
