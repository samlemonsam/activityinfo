package org.activityinfo.server.endpoint.odk;

import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.NarrativeType;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.image.ImageType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.time.LocalDateType;

public class FieldValueParserFactory {
    static public FieldValueParser fromFieldType(FieldType fieldType, boolean odk, boolean legacy) {
        if (fieldType instanceof BarcodeType) return new BarcodeFieldValueParser();
        if (fieldType instanceof BooleanType) return new BooleanFieldValueParser();
        if (fieldType instanceof EnumType) {
            if (odk) {
                if (legacy) {
                    return new LegacyEnumFieldValueParser();
                } else {
                    return new IdEnumFieldValueParser();
                }
            } else {
                return new CodeEnumFieldValueParser((EnumType) fieldType);
            }
        }
        if (fieldType instanceof GeoPointType) return new GeoPointFieldValueParser();
        if (fieldType instanceof ImageType) return new ImageFieldValueParser();
        if (fieldType instanceof LocalDateType) return new LocalDateFieldValueParser();
        if (fieldType instanceof NarrativeType) {
            if (legacy) {
                return new TextFieldValueParser();
            } else {
                return new NarrativeFieldValueParser();
            }
        }
        if (fieldType instanceof QuantityType) return new QuantityFieldValueParser((QuantityType) fieldType);
        if (fieldType instanceof ReferenceType) {
            if (legacy) {
                return new LegacyReferenceFieldValueParser(((ReferenceType) fieldType).getRange());
            } else {
                return new ReferenceFieldValueParser();
            }
        }
        if (fieldType instanceof TextType) return new TextFieldValueParser();

        // If this happens, it means this class needs to be expanded to support the new FieldType class.
        throw new IllegalArgumentException("Unknown FieldType passed to FieldValueParserFactory.get()");
    }
}
