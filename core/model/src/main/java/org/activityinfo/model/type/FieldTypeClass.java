package org.activityinfo.model.type;

import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.time.LocalDateType;

/**
 * Defines a class of Field Types.
 *
 * FieldTypeClass are meant to provide a very specific types of fields
 * at a high level of abstraction. For example, beyond simply a "number" type,
 * we will also have a QuantityType, a RatioType, a CurrencyType, etc, that carry
 * logic with them about how they should be aggregated, indexed, etc.
 *
 * FieldTypeClasses can be further specialized with parameters: for example, the
 * QuantityType takes a "units" parameter.
 *
 */
public interface FieldTypeClass {

    public static final String TYPE_FIELD_NAME = "type";

    /**
     *
     * @return a string uniquely identifying this {@code FieldTypeClass}. This
     * identifier will be stored with all values of types in this class.
     */
    String getId();


    /**
     *
     * @return an instance of this {@code FieldTypeClass} using default parameters
     */
    FieldType createType();


    // intermediate step to support refactoring


    public static final ParametrizedFieldTypeClass QUANTITY = QuantityType.TYPE_CLASS;

    public static final FieldTypeClass NARRATIVE = NarrativeType.TYPE_CLASS;

    public static final FieldTypeClass FREE_TEXT = TextType.TYPE_CLASS;

    public static final FieldTypeClass LOCAL_DATE = LocalDateType.TYPE_CLASS;

    public static final FieldTypeClass GEOGRAPHIC_POINT = GeoPointType.TYPE_CLASS;

    public static final FieldTypeClass BOOLEAN = BooleanType.TYPE_CLASS;

    public static final FieldTypeClass BARCODE = BarcodeType.TYPE_CLASS;

    public static final ParametrizedFieldTypeClass REFERENCE = ReferenceType.TYPE_CLASS;

}
