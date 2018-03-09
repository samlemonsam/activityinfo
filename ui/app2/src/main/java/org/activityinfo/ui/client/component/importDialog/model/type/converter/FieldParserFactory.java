/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.component.importDialog.model.type.converter;

import org.activityinfo.io.match.coord.CoordinateParser;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.NarrativeType;
import org.activityinfo.model.type.NarrativeValue;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.ui.client.component.importDialog.model.type.formatter.QuantityFormatterFactory;

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
