package org.activityinfo.ui.client.input.view.field;

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

public class FieldWidgetFactory implements FieldTypeVisitor<FieldWidget> {

    private FieldUpdater updater;

    public static FieldWidget create(FieldType type, FieldUpdater updater) {
        return type.accept(new FieldWidgetFactory(updater));
    }

    private FieldWidgetFactory(FieldUpdater updater) {
        this.updater = updater;
    }

    @Override
    public FieldWidget visitAttachment(AttachmentType attachmentType) {
        return null;
    }

    @Override
    public FieldWidget visitCalculated(CalculatedFieldType calculatedFieldType) {
        return null;
    }

    @Override
    public FieldWidget visitReference(ReferenceType referenceType) {
        return null;
    }

    @Override
    public FieldWidget visitNarrative(NarrativeType narrativeType) {
        return null;
    }

    @Override
    public FieldWidget visitBoolean(BooleanType booleanType) {
        return null;
    }

    @Override
    public FieldWidget visitQuantity(QuantityType type) {
        return null;
    }

    @Override
    public FieldWidget visitGeoPoint(GeoPointType geoPointType) {
        return null;
    }

    @Override
    public FieldWidget visitGeoArea(GeoAreaType geoAreaType) {
        return null;
    }

    @Override
    public FieldWidget visitEnum(EnumType enumType) {
        return new EnumWidget(enumType, updater);
    }

    @Override
    public FieldWidget visitBarcode(BarcodeType barcodeType) {
        return null;
    }

    @Override
    public FieldWidget visitSubForm(SubFormReferenceType subFormReferenceType) {
        return null;
    }

    @Override
    public FieldWidget visitLocalDate(LocalDateType localDateType) {
        return null;
    }

    @Override
    public FieldWidget visitMonth(MonthType monthType) {
        return null;
    }

    @Override
    public FieldWidget visitYear(YearType yearType) {
        return null;
    }

    @Override
    public FieldWidget visitLocalDateInterval(LocalDateIntervalType localDateIntervalType) {
        return null;
    }

    @Override
    public FieldWidget visitText(TextType textType) {
        return new TextWidget(updater);
    }

    @Override
    public FieldWidget visitSerialNumber(SerialNumberType serialNumberType) {
        return null;
    }
}
