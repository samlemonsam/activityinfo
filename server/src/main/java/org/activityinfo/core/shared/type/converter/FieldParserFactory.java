package org.activityinfo.core.shared.type.converter;

import org.activityinfo.core.shared.type.formatter.QuantityFormatterFactory;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.NarrativeType;
import org.activityinfo.model.type.NarrativeValue;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.time.LocalDateType;

import javax.annotation.Nonnull;

/**
 * Provides Converters between supported types.
 */
public class FieldParserFactory {


    private QuantityFormatterFactory quantityFormatterFactory;
    private CoordinateParser.NumberFormatter coordinateNumberFormatter;

    public FieldParserFactory(QuantityFormatterFactory quantityFormatterFactory,
                              CoordinateParser.NumberFormatter coordinateNumberFormatter) {
        this.quantityFormatterFactory = quantityFormatterFactory;
        this.coordinateNumberFormatter = coordinateNumberFormatter;
    }

    public FieldValueParser createStringConverter(FieldType fieldType) {
        if (fieldType instanceof QuantityType) {
            return new QuantityParser((QuantityType) fieldType, quantityFormatterFactory.create());

        } else if (fieldType instanceof LocalDateType) {
            return new LocalDateParser();

        } else if (fieldType instanceof TextType) {
            return new FieldValueParser() {
                @Override
                public FieldValue convert(@Nonnull String value) {
                    return TextValue.valueOf(value);
                }
            };
        } else if (fieldType instanceof NarrativeType) {
            return new FieldValueParser() {
                @Override
                public FieldValue convert(@Nonnull String value) {
                    return NarrativeValue.valueOf(value);
                }
            };
        }
        throw new UnsupportedOperationException(fieldType.toString());
    }

    public CoordinateParser.NumberFormatter getCoordinateNumberFormatter() {
        return coordinateNumberFormatter;
    }
}
