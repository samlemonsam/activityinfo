package org.activityinfo.store.query.shared.columns;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.model.type.attachment.AttachmentValue;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.HasStringValue;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.model.type.time.*;
import org.activityinfo.store.query.shared.PendingSlot;
import org.activityinfo.store.spi.CursorObserver;


public class ViewBuilderFactory implements FieldTypeVisitor<CursorObserver<FieldValue>> {

    private final PendingSlot<ColumnView> result;
    private final ColumnFactory factory;

    private ViewBuilderFactory(PendingSlot<ColumnView> result, ColumnFactory factory) {
        this.result = result;
        this.factory = factory;
    }

    public static CursorObserver<FieldValue> get(ColumnFactory factory, PendingSlot<ColumnView> result, FieldType type) {
        return type.accept(new ViewBuilderFactory(result, factory));
    }

    @Override
    public CursorObserver<FieldValue> visitAttachment(AttachmentType attachmentType) {
        return factory.newStringBuilder(result, new AttachmentBlobIdReader());
    }

    @Override
    public CursorObserver<FieldValue> visitCalculated(CalculatedFieldType calculatedFieldType) {
        // Calculated fields should be expanded by the time we get here.
        throw new IllegalStateException();
    }

    @Override
    public CursorObserver<FieldValue> visitReference(ReferenceType referenceType) {
        return factory.newStringBuilder(result, new ReferenceIdReader());
    }

    @Override
    public CursorObserver<FieldValue> visitNarrative(NarrativeType narrativeType) {
        return factory.newStringBuilder(result, new TextFieldReader());
    }

    @Override
    public CursorObserver<FieldValue> visitBoolean(BooleanType booleanType) {
        return new BooleanColumnBuilder(result);
    }

    @Override
    public CursorObserver<FieldValue> visitQuantity(QuantityType type) {
        return factory.newDoubleBuilder(result, new QuantityReader());
    }

    @Override
    public CursorObserver<FieldValue> visitGeoPoint(GeoPointType geoPointType) {
        return new UnsupportedColumnTypeBuilder(result);
    }

    @Override
    public CursorObserver<FieldValue> visitGeoArea(GeoAreaType geoAreaType) {
        return new UnsupportedColumnTypeBuilder(result);
    }

    @Override
    public CursorObserver<FieldValue> visitEnum(EnumType enumType) {
        return factory.newEnumBuilder(result, enumType);
    }

    @Override
    public CursorObserver<FieldValue> visitBarcode(BarcodeType barcodeType) {
        return factory.newStringBuilder(result, new TextFieldReader());
    }

    @Override
    public CursorObserver<FieldValue> visitSubForm(SubFormReferenceType subFormReferenceType) {
        return new UnsupportedColumnTypeBuilder(result);
    }

    @Override
    public CursorObserver<FieldValue> visitLocalDate(LocalDateType localDateType) {
        return factory.newStringBuilder(result, new ToStringReader());
    }

    @Override
    public CursorObserver<FieldValue> visitMonth(MonthType monthType) {
        return factory.newStringBuilder(result, new ToStringReader());
    }

    @Override
    public CursorObserver<FieldValue> visitYear(YearType yearType) {
        return factory.newDoubleBuilder(result, new DoubleReader() {
            @Override
            public double read(FieldValue value) {
                if(value instanceof YearValue) {
                    return ((YearValue) value).getYear();
                }
                return Double.NaN;
            }
        });
    }

    @Override
    public CursorObserver<FieldValue> visitFortnight(FortnightType fortnightType) {
        return factory.newStringBuilder(result, new ToStringReader());
    }

    @Override
    public CursorObserver<FieldValue> visitWeek(EpiWeekType epiWeekType) {
        return factory.newStringBuilder(result, new ToStringReader());
    }

    @Override
    public CursorObserver<FieldValue> visitLocalDateInterval(LocalDateIntervalType localDateIntervalType) {
        return new UnsupportedColumnTypeBuilder(result);
    }

    @Override
    public CursorObserver<FieldValue> visitText(TextType textType) {
        return factory.newStringBuilder(result, new TextFieldReader());
    }

    @Override
    public CursorObserver<FieldValue> visitSerialNumber(SerialNumberType serialNumberType) {
        return factory.newStringBuilder(result, new SerialNumberReader(serialNumberType));
    }

    private static class TextFieldReader implements StringReader {
        @Override
        public String readString(FieldValue value) {
            if(value instanceof HasStringValue) {
                return ((HasStringValue) value).asString();
            }
            return null;
        }
    }

    private static class ToStringReader implements StringReader {

        @Override
        public String readString(FieldValue value) {
            if(value == null) {
                return null;
            }
            return value.toString();
        }
    }

    private static class QuantityReader implements DoubleReader {
        @Override
        public double read(FieldValue fieldValue) {
            if(fieldValue instanceof Quantity) {
                Quantity quantity = (Quantity) fieldValue;
                return quantity.getValue();
            } else {
                return Double.NaN;
            }
        }
    }

    private static class ReferenceIdReader implements StringReader {
        @Override
        public String readString(FieldValue value) {
            if(value instanceof ReferenceValue) {
                ReferenceValue ref = (ReferenceValue) value;
                if(ref.getReferences().size() == 1) {
                    return ref.getOnlyReference().getRecordId().asString();
                }
            }
            return null;
        }
    }

    private static class AttachmentBlobIdReader implements StringReader {
        @Override
        public String readString(FieldValue value) {
            if(value instanceof AttachmentValue) {
                AttachmentValue imageValue = (AttachmentValue) value;
                if(imageValue.getValues().size() >= 1) {
                    return imageValue.getValues().get(0).getBlobId();
                }
            }
            return null;
        }
    }
}
