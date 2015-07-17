package org.activityinfo.model.form;

import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.NarrativeType;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.time.LocalDateType;

/**
 * The type of field, which influences how input is presented
 * the user, how it is validated, and what default measures
 * are available.
 */
public class FormFieldType {

    private FormFieldType() {}


    /**
     * Defined exact length of string to differ between FREE_TEXT and NARRATIVE types.
     * If string length less than #FREE_TEXT_LENGTH then type is #FREE_TEXT otherwise it is NARRATIVE.
     */
    public static final int FREE_TEXT_LENGTH = 80;


    public static FieldTypeClass valueOf(String name) {
        switch(name) {
            case "QUANTITY":
                return QuantityType.TYPE_CLASS;
            case "NARRATIVE":
                return NarrativeType.TYPE_CLASS;
            case "FREE_TEXT":
                return TextType.TYPE_CLASS;
            case "LOCAL_DATE":
                return LocalDateType.TYPE_CLASS;
            case "GEOGRAPHIC_POINT":
                return GeoPointType.TYPE_CLASS;
            case "REFERENCE":
                return ReferenceType.TYPE_CLASS;
            case "BOOLEAN":
                return BooleanType.TYPE_CLASS;
            case "BARCODE":
                return BarcodeType.TYPE_CLASS;
        }
        throw new IllegalArgumentException("name: " + name);
    }

    public static FieldTypeClass[] values() {
        return new FieldTypeClass[] {
                FieldTypeClass.QUANTITY,
                FieldTypeClass.NARRATIVE,
                FieldTypeClass.FREE_TEXT,
                FieldTypeClass.LOCAL_DATE,
                FieldTypeClass.BOOLEAN,
                FieldTypeClass.GEOGRAPHIC_POINT,
                FieldTypeClass.BARCODE};

    }
}
