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
package org.activityinfo.model.type;

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

public interface FieldTypeVisitor<T> {
    T visitAttachment(AttachmentType attachmentType);

    T visitCalculated(CalculatedFieldType calculatedFieldType);

    T visitReference(ReferenceType referenceType);

    T visitNarrative(NarrativeType narrativeType);

    T visitBoolean(BooleanType booleanType);

    T visitQuantity(QuantityType type);

    T visitGeoPoint(GeoPointType geoPointType);

    T visitGeoArea(GeoAreaType geoAreaType);

    T visitEnum(EnumType enumType);

    T visitBarcode(BarcodeType barcodeType);

    T visitSubForm(SubFormReferenceType subFormReferenceType);

    T visitLocalDate(LocalDateType localDateType);

    T visitMonth(MonthType monthType);

    T visitYear(YearType yearType);

    T visitFortnight(FortnightType fortnightType);

    T visitWeek(EpiWeekType epiWeekType);

    T visitLocalDateInterval(LocalDateIntervalType localDateIntervalType);

    T visitText(TextType textType);

    T visitSerialNumber(SerialNumberType serialNumberType);


}
