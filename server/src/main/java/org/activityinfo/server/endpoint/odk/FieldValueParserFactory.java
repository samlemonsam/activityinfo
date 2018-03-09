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
package org.activityinfo.server.endpoint.odk;

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
import org.activityinfo.model.type.time.*;

public class FieldValueParserFactory implements FieldTypeVisitor<FieldValueParser> {

    public static OdkFieldValueParser create(FieldType type) {
        return new OdkFieldValueParser(type.accept(new FieldValueParserFactory()));
    }

    @Override
    public FieldValueParser visitAttachment(AttachmentType attachmentType) {
        return new AttachmentFieldValueParser();
    }

    @Override
    public FieldValueParser visitCalculated(CalculatedFieldType calculatedFieldType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldValueParser visitReference(ReferenceType referenceType) {
        return new ReferenceFieldValueParser();
    }

    @Override
    public FieldValueParser visitNarrative(NarrativeType narrativeType) {
        return new NarrativeFieldValueParser();
    }

    @Override
    public FieldValueParser visitBoolean(BooleanType booleanType) {
        return new BooleanFieldValueParser();
    }

    @Override
    public FieldValueParser visitQuantity(QuantityType type) {
        return new QuantityFieldValueParser(type);
    }

    @Override
    public FieldValueParser visitGeoPoint(GeoPointType geoPointType) {
        return new GeoPointFieldValueParser();
    }

    @Override
    public FieldValueParser visitGeoArea(GeoAreaType geoAreaType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldValueParser visitEnum(EnumType enumType) {
        return new IdEnumFieldValueParser();
    }

    @Override
    public FieldValueParser visitBarcode(BarcodeType barcodeType) {
        return new BarcodeFieldValueParser();
    }

    @Override
    public FieldValueParser visitSubForm(SubFormReferenceType subFormReferenceType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldValueParser visitLocalDate(LocalDateType localDateType) {
        return new LocalDateFieldValueParser();
    }

    @Override
    public FieldValueParser visitMonth(MonthType monthType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldValueParser visitWeek(EpiWeekType epiWeekType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldValueParser visitYear(YearType yearType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldValueParser visitFortnight(FortnightType fortnightType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldValueParser visitLocalDateInterval(LocalDateIntervalType localDateIntervalType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldValueParser visitText(TextType textType) {
        return new TextFieldValueParser();
    }

    @Override
    public FieldValueParser visitSerialNumber(SerialNumberType serialNumberType) {
        throw new UnsupportedOperationException();
    }
}