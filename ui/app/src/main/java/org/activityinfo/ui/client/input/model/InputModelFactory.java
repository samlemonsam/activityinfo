package org.activityinfo.ui.client.input.model;

import org.activityinfo.model.type.FieldTypeVisitor;
import org.activityinfo.model.type.SerialNumberType;
import org.activityinfo.model.type.NarrativeType;
import org.activityinfo.model.type.ReferenceType;
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


public enum InputModelFactory implements FieldTypeVisitor<InputModel> {

    INSTANCE;



    @Override
    public InputModel visitAttachment(AttachmentType attachmentType) {
        return new SimpleInputModel();
    }

    @Override
    public InputModel visitCalculated(CalculatedFieldType calculatedFieldType) {
        return new SimpleInputModel();
    }

    @Override
    public InputModel visitReference(ReferenceType referenceType) {
        return new ReferenceInput();
    }

    @Override
    public InputModel visitNarrative(NarrativeType narrativeType) {
        return new NarrativeInput();
    }

    @Override
    public InputModel visitBoolean(BooleanType booleanType) {
        return new SimpleInputModel();
    }

    @Override
    public InputModel visitQuantity(QuantityType type) {
        return new QuantityInput();
    }

    @Override
    public InputModel visitGeoPoint(GeoPointType geoPointType) {
        return new GeoPointInput();
    }

    @Override
    public InputModel visitGeoArea(GeoAreaType geoAreaType) {
        return new SimpleInputModel();
    }

    @Override
    public InputModel visitEnum(EnumType enumType) {
        return new EnumInput();
    }

    @Override
    public InputModel visitBarcode(BarcodeType barcodeType) {
        return new BarcodeInput();
    }

    @Override
    public InputModel visitSubForm(SubFormReferenceType subFormReferenceType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputModel visitLocalDate(LocalDateType localDateType) {
        return new LocalDateInput();
    }

    @Override
    public InputModel visitMonth(MonthType monthType) {
        return new SimpleInputModel();
    }

    @Override
    public InputModel visitYear(YearType yearType) {
        return new SimpleInputModel();
    }

    @Override
    public InputModel visitLocalDateInterval(LocalDateIntervalType localDateIntervalType) {
        return new SimpleInputModel();
    }

    @Override
    public InputModel visitText(TextType textType) {
        return new SimpleInputModel();
    }

    @Override
    public InputModel visitFileNumber(SerialNumberType serialNumberType) {
        throw new UnsupportedOperationException();
    }


}
