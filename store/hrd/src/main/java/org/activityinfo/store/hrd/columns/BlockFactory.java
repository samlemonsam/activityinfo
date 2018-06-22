package org.activityinfo.store.hrd.columns;

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

    public static BlockManager get(FieldType type) {
        return type.accept(new BlockFactory());
    }

    @Override
    public BlockManager visitAttachment(AttachmentType attachmentType) {
        return new StringBlock(new ViewBuilderFactory.AttachmentBlobIdReader());
    }

    @Override
    public BlockManager visitCalculated(CalculatedFieldType calculatedFieldType) {
        // Calculated fields are not stored.
        throw new IllegalStateException();
    }

    @Override
    public BlockManager visitReference(ReferenceType referenceType) {
        return new StringBlock(new ViewBuilderFactory.ReferenceIdReader());
    }

    @Override
    public BlockManager visitNarrative(NarrativeType narrativeType) {
        return new StringBlock(new ViewBuilderFactory.ReferenceIdReader());
    }

    @Override
    public BlockManager visitBoolean(BooleanType booleanType) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public BlockManager visitQuantity(QuantityType type) {
        return new NumberBlock((DoubleReader) value -> ((Quantity) value).getValue());
    }

    @Override
    public BlockManager visitGeoPoint(GeoPointType geoPointType) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public BlockManager visitGeoArea(GeoAreaType geoAreaType) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public BlockManager visitEnum(EnumType enumType) {
        if(enumType.getCardinality() == Cardinality.MULTIPLE) {
            return new MultiEnumBlock();
        } else {
            return new SingleEnumBlock(enumType);
        }
    }

    @Override
    public BlockManager visitBarcode(BarcodeType barcodeType) {
        return new StringBlock(new ViewBuilderFactory.TextFieldReader());
    }

    @Override
    public BlockManager visitSubForm(SubFormReferenceType subFormReferenceType) {
        return NullBlock.INSTANCE;
    }

    @Override
    public BlockManager visitLocalDate(LocalDateType localDateType) {
        return NullBlock.INSTANCE;
    }

    @Override
    public BlockManager visitMonth(MonthType monthType) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public BlockManager visitYear(YearType yearType) {
        return new NumberBlock((IntReader) value -> ((YearValue) value).getYear());
    }

    @Override
    public BlockManager visitFortnight(FortnightType fortnightType) {
        return NullBlock.INSTANCE;
    }

    @Override
    public BlockManager visitWeek(EpiWeekType epiWeekType) {
        return NullBlock.INSTANCE;
    }

    @Override
    public BlockManager visitLocalDateInterval(LocalDateIntervalType localDateIntervalType) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public BlockManager visitText(TextType textType) {
        return new StringBlock(new ViewBuilderFactory.TextFieldReader());
    }

    @Override
    public BlockManager visitSerialNumber(SerialNumberType serialNumberType) {
        return NullBlock.INSTANCE;
    }

}
