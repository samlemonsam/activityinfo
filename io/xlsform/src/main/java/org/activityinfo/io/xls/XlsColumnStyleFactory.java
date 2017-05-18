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
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;

public class XlsColumnStyleFactory implements FieldTypeVisitor<CellStyle> {

    private final CellStyle textStyle;
    private final CellStyle dateStyle;

    public XlsColumnStyleFactory(Workbook book) {

        textStyle = book.createCellStyle();

        dateStyle = book.createCellStyle();
        dateStyle.setDataFormat(book.getCreationHelper().createDataFormat().getFormat("yyyy-mm-dd"));
    }

    @Override
    public CellStyle visitAttachment(AttachmentType attachmentType) {
        return textStyle;
    }

    @Override
    public CellStyle visitCalculated(CalculatedFieldType calculatedFieldType) {
        return textStyle;
    }

    @Override
    public CellStyle visitReference(ReferenceType referenceType) {
        return textStyle;
    }

    @Override
    public CellStyle visitNarrative(NarrativeType narrativeType) {
        return textStyle;
    }

    @Override
    public CellStyle visitBoolean(BooleanType booleanType) {
        return textStyle;
    }

    @Override
    public CellStyle visitQuantity(QuantityType type) {
        return textStyle;
    }

    @Override
    public CellStyle visitGeoPoint(GeoPointType geoPointType) {
        return textStyle;
    }

    @Override
    public CellStyle visitGeoArea(GeoAreaType geoAreaType) {
        return textStyle;
    }

    @Override
    public CellStyle visitEnum(EnumType enumType) {
        return textStyle;
    }

    @Override
    public CellStyle visitBarcode(BarcodeType barcodeType) {
        return textStyle;
    }

    @Override
    public CellStyle visitSubForm(SubFormReferenceType subFormReferenceType) {
        return textStyle;
    }

    @Override
    public CellStyle visitLocalDate(LocalDateType localDateType) {
        return dateStyle;
    }

    @Override
    public CellStyle visitMonth(MonthType monthType) {
        return textStyle;
    }

    @Override
    public CellStyle visitYear(YearType yearType) {
        return textStyle;
    }

    @Override
    public CellStyle visitLocalDateInterval(LocalDateIntervalType localDateIntervalType) {
        return textStyle;
    }

    @Override
    public CellStyle visitText(TextType textType) {
        return textStyle;
    }

    @Override
    public CellStyle visitSerialNumber(SerialNumberType serialNumberType) {
        return textStyle;
    }

    public CellStyle create(FieldType type) {
        return type.accept(this);
    }
}
