package org.activityinfo.store.hrd.columns;

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.model.type.time.*;
import org.activityinfo.store.query.shared.columns.DoubleReader;
import org.activityinfo.store.query.shared.columns.IntReader;
import org.activityinfo.store.query.shared.columns.ViewBuilderFactory;

public class BlockFactory implements FieldTypeVisitor<BlockManager> {

    private final String fieldName;

    public BlockFactory(FormField field) {
        this.fieldName = field.getName();
    }

    public static BlockManager get(FormField field) {
        return field.getType().accept(new BlockFactory(field));
    }

    @Override
    public BlockManager visitAttachment(AttachmentType attachmentType) {
        return new StringBlock(fieldName, new ViewBuilderFactory.AttachmentBlobIdReader());
    }

    @Override
    public BlockManager visitCalculated(CalculatedFieldType calculatedFieldType) {
        // Calculated fields are not stored.
        throw new IllegalStateException();
    }

    @Override
    public BlockManager visitReference(ReferenceType referenceType) {
        return new StringBlock(fieldName, new ViewBuilderFactory.ReferenceIdReader());
    }

    @Override
    public BlockManager visitNarrative(NarrativeType narrativeType) {
        return new StringBlock(fieldName, new ViewBuilderFactory.TextFieldReader());
    }

    @Override
    public BlockManager visitBoolean(BooleanType booleanType) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public BlockManager visitQuantity(QuantityType type) {
        return new NumberBlock(fieldName, (DoubleReader) value -> ((Quantity) value).getValue());
    }

    @Override
    public BlockManager visitGeoPoint(GeoPointType geoPointType) {
        return new GeoPointBlock(fieldName);
    }

    @Override
    public BlockManager visitGeoArea(GeoAreaType geoAreaType) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public BlockManager visitEnum(EnumType enumType) {
        if(enumType.getCardinality() == Cardinality.MULTIPLE) {
            return new MultiEnumBlock(fieldName);
        } else {
            return new SingleEnumBlock(fieldName, enumType);
        }
    }

    @Override
    public BlockManager visitBarcode(BarcodeType barcodeType) {
        return new StringBlock(fieldName, new ViewBuilderFactory.TextFieldReader());
    }

    @Override
    public BlockManager visitSubForm(SubFormReferenceType subFormReferenceType) {
        return NullBlock.INSTANCE;
    }

    @Override
    public BlockManager visitLocalDate(LocalDateType localDateType) {
        IntReader reader = new IntReader() {
            @Override
            public int read(FieldValue value) {
                return DateEncoding.encodeLocalDate(((LocalDate) value));
            }
        };
        NumberBlock.IntColumnFactory columnFactory = new NumberBlock.IntColumnFactory() {
            @Override
            public ColumnView create(int[] values) {
                return new DateColumnView(values, new LocalDateRender());
            }
        };
        return new NumberBlock(fieldName, reader, columnFactory);
    }

    @Override
    public BlockManager visitMonth(MonthType monthType) {
        IntReader reader = new IntReader() {
            @Override
            public int read(FieldValue value) {
                return DateEncoding.encodeMonth(((Month) value));
            }
        };

        NumberBlock.IntColumnFactory columnFactory = new NumberBlock.IntColumnFactory() {
            @Override
            public ColumnView create(int[] values) {
                return new DateColumnView(values, new MonthStringRenderer());
            }
        };

        return new NumberBlock(fieldName, reader, columnFactory);
    }

    @Override
    public BlockManager visitYear(YearType yearType) {
        return new NumberBlock(fieldName, (IntReader) value -> ((YearValue) value).getYear());
    }

    @Override
    public BlockManager visitFortnight(FortnightType fortnightType) {
        IntReader reader = new IntReader() {
            @Override
            public int read(FieldValue value) {
                return DateEncoding.encodeFortnight((FortnightValue) value);
            }
        };

        NumberBlock.IntColumnFactory columnFactory = new NumberBlock.IntColumnFactory() {
            @Override
            public ColumnView create(int[] values) {
                return new DateColumnView(values, new FortnightStringRenderer());
            }
        };

        return new NumberBlock(fieldName, reader, columnFactory);
    }

    @Override
    public BlockManager visitWeek(EpiWeekType epiWeekType) {
        IntReader reader = new IntReader() {
            @Override
            public int read(FieldValue value) {
                return DateEncoding.encodeWeek(((EpiWeek) value));
            }
        };

        NumberBlock.IntColumnFactory columnFactory = new NumberBlock.IntColumnFactory() {
            @Override
            public ColumnView create(int[] values) {
                return new DateColumnView(values, new WeekColumnRenderer());
            }
        };

        return new NumberBlock(fieldName, reader, columnFactory);
    }

    @Override
    public BlockManager visitLocalDateInterval(LocalDateIntervalType localDateIntervalType) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public BlockManager visitText(TextType textType) {
        return new StringBlock(fieldName, new ViewBuilderFactory.TextFieldReader());
    }

    @Override
    public BlockManager visitSerialNumber(SerialNumberType serialNumberType) {
        return new SerialNumberBlock(serialNumberType);
    }

}
