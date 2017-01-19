package org.activityinfo.store.query.impl.builders;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.model.type.attachment.AttachmentValue;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.barcode.BarcodeValue;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.service.store.CursorObserver;
import org.activityinfo.store.query.impl.PendingSlot;

import java.util.logging.Logger;

public class ViewBuilderFactory {

    private static final ViewBuilderFactory INSTANCE = new ViewBuilderFactory();
    private static final Logger LOGGER = Logger.getLogger(ViewBuilderFactory.class.getName());

    private ViewBuilderFactory() {}


    public static CursorObserver<FieldValue> get(PendingSlot<ColumnView> result, FieldType type) {
        if(type instanceof TextType) {
            return new StringColumnBuilder(result, new TextFieldReader());
        } else if(type instanceof NarrativeType) {
            return new StringColumnBuilder(result, new NarrativeFieldReader());
        } else if(type instanceof QuantityType) {
            return new DoubleColumnBuilder(result, new QuantityReader());
        } else if(type instanceof BarcodeType) {
            return new StringColumnBuilder(result, new BarcodeReader());
        } else if(type instanceof ReferenceType) {
            return new StringColumnBuilder(result, new ReferenceIdReader());
        } else if(type instanceof EnumType) {
            return new EnumColumnBuilder(result, (EnumType) type);
        } else if(type instanceof BooleanType) {
            return new BooleanColumnBuilder(result);
        } else if(type instanceof LocalDateType) {
            return new StringColumnBuilder(result, new LocalDateReader());
        } else if(type instanceof AttachmentType) {
            return new StringColumnBuilder(result, new AttachmentBlobIdReader());
        } else {
            return new UnsupportedColumnTypeBuilder(result);
        }
    }

    private static class TextFieldReader implements StringReader {
        @Override
        public String readString(FieldValue value) {
            if(value instanceof TextValue) {
                return ((TextValue) value).asString();
            }
            return null;
        }
    }


    private static class NarrativeFieldReader implements StringReader {
        @Override
        public String readString(FieldValue value) {
            if(value instanceof NarrativeValue) {
                return ((NarrativeValue) value).asString();
            }
            return null;
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

    private static class BarcodeReader implements StringReader {
        @Override
        public String readString(FieldValue value) {
            if(value instanceof BarcodeValue) {
                return ((BarcodeValue) value).asString();
            }
            return null;
        }
    }

    private static class LocalDateReader implements StringReader {
        @Override
        public String readString(FieldValue value) {
            if(value instanceof LocalDate) {
                return value.toString();
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
