package org.activityinfo.model.type;

import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.model.type.time.LocalDateIntervalType;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.model.type.time.MonthType;
import org.activityinfo.model.type.time.YearType;

public interface FieldTypeVisitor<T> {
    T visitAttachment(AttachmentType attachmentType);

    T visitCalculated(CalculatedFieldType calculatedFieldType);

    T visitReference(ReferenceType referenceType);

    T visitNarrative(NarrativeType narrativeType);

    T visitBoolean(BooleanType booleanType);

    T visitQuantity(QuantityType type);

    T visitGeoPoint(GeoPointType geoPointType);

    T visitGeoArea(GeoAreaType geoAreaType);

    T visitEnum(EnumType enumType);

    T visitBarcode(BarcodeType barcodeType);

    T visitSubForm(SubFormReferenceType subFormReferenceType);

    T visitLocalDate(LocalDateType localDateType);

    T visitMonth(MonthType monthType);

    T visitYear(YearType yearType);

    T visitLocalDateInterval(LocalDateIntervalType localDateIntervalType);

    T visitText(TextType textType);

    T visitFileNumber(FileNumberType fileNumberType);

}
