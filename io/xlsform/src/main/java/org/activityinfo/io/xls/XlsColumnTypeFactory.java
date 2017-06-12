package org.activityinfo.io.xls;

import org.activityinfo.model.type.*;
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


public class XlsColumnTypeFactory implements FieldTypeVisitor<XlsColumnType> {

    private XlsColumnTypeFactory() {}

    public static XlsColumnType get(FieldType type) {
        return type.accept(new XlsColumnTypeFactory());
    }


    @Override
    public XlsColumnType visitAttachment(AttachmentType attachmentType) {
        return XlsColumnType.EMPTY;
    }

    @Override
    public XlsColumnType visitCalculated(CalculatedFieldType calculatedFieldType) {
        throw new IllegalStateException("Should have been resolved to concrete type");
    }

    @Override
    public XlsColumnType visitReference(ReferenceType referenceType) {
        return XlsColumnType.STRING;
    }

    @Override
    public XlsColumnType visitNarrative(NarrativeType narrativeType) {
        return XlsColumnType.STRING;
    }

    @Override
    public XlsColumnType visitBoolean(BooleanType booleanType) {
        return XlsColumnType.BOOLEAN;
    }

    @Override
    public XlsColumnType visitQuantity(QuantityType type) {
        return XlsColumnType.NUMBER;
    }

    @Override
    public XlsColumnType visitGeoPoint(GeoPointType geoPointType) {
        return XlsColumnType.EMPTY;
    }

    @Override
    public XlsColumnType visitGeoArea(GeoAreaType geoAreaType) {
        return XlsColumnType.EMPTY;
    }

    @Override
    public XlsColumnType visitEnum(EnumType enumType) {
        return XlsColumnType.STRING;
    }

    @Override
    public XlsColumnType visitBarcode(BarcodeType barcodeType) {
        return XlsColumnType.STRING;
    }

    @Override
    public XlsColumnType visitSubForm(SubFormReferenceType subFormReferenceType) {
        return XlsColumnType.EMPTY;
    }

    @Override
    public XlsColumnType visitLocalDate(LocalDateType localDateType) {
        return XlsColumnType.DATE;
    }

    @Override
    public XlsColumnType visitMonth(MonthType monthType) {
        return XlsColumnType.NUMBER;
    }

    @Override
    public XlsColumnType visitYear(YearType yearType) {
        return XlsColumnType.NUMBER;
    }

    @Override
    public XlsColumnType visitLocalDateInterval(LocalDateIntervalType localDateIntervalType) {
        return XlsColumnType.DATE;
    }

    @Override
    public XlsColumnType visitText(TextType textType) {
        return XlsColumnType.STRING;
    }

    @Override
    public XlsColumnType visitSerialNumber(SerialNumberType serialNumberType) {
        return XlsColumnType.STRING;
    }

}
