package org.activityinfo.store.query.impl.builders;

import org.activityinfo.model.type.*;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.barcode.BarcodeValue;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.image.ImageType;
import org.activityinfo.model.type.image.ImageValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.time.*;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ViewBuilderFactory {

    private static final ViewBuilderFactory INSTANCE = new ViewBuilderFactory();
    private static final Logger LOGGER = Logger.getLogger(ViewBuilderFactory.class.getName());

    private ViewBuilderFactory() {}


    public static ColumnViewBuilder get(FieldType type) {
        if(type instanceof TextType) {
            return new StringColumnBuilder(new TextFieldReader());
        } else if(type instanceof NarrativeType) {
            return new StringColumnBuilder(new NarrativeFieldReader());
        } else if(type instanceof QuantityType) {
            return new DoubleColumnBuilder(new QuantityReader());
        } else if(type instanceof BarcodeType) {
            return new StringColumnBuilder(new BarcodeReader());
        } else if(type instanceof ReferenceType) {
            return new StringColumnBuilder(new ReferenceIdReader());
        } else if(type instanceof EnumType) {
            return new EnumColumnBuilder((EnumType) type);
        } else if(type instanceof BooleanType) {
            return new BooleanColumnBuilder();
        } else if(type instanceof LocalDateType) {
            return new DateColumnBuilder(new LocalDateReader());
        } else if(type instanceof ImageType) {
            return new StringColumnBuilder(new ImageBlobIdReader());
        } else {
            LOGGER.log(Level.SEVERE, "Unsupported type: " + type);
            return null;
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
                if(ref.getResourceIds().size() == 1) {
                    return ref.getResourceId().asString();
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

    private static class LocalDateReader implements DateReader {
        @Override
        public Date readDate(FieldValue value) {
            if(value instanceof LocalDate) {
                return ((LocalDate) value).atMidnightInMyTimezone();
            }
            return null;
        }
    }
//
//    private static class YearReader implements DoubleReader {
//        @Override
//        public double read(FieldValue value) {
//            if(value instanceof YearValue) {
//                return ((YearValue)value).getYear();
//            } else {
//                return Double.NaN;
//            }
//        }
//    }
//
//    private static class MonthReader implements DateReader {
//        @Override
//        public Date readDate(FieldValue value) {
//            if(value instanceof TemporalValue) {
//                return ((TemporalValue) value).asInterval().getEndDate().atMidnightInMyTimezone();
//            }
//            return null;
//        }
//    }

    private static class ImageBlobIdReader implements StringReader {
        @Override
        public String readString(FieldValue value) {
            if(value instanceof ImageValue) {
                ImageValue imageValue = (ImageValue) value;
                if(imageValue.getValues().size() >= 1) {
                    return imageValue.getValues().get(0).getBlobId();
                }
            }
            return null;
        }
    }
}
