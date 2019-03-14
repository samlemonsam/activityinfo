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
package org.activityinfo.ui.client.icons;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
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

public interface IconBundle extends ClientBundle {

    IconBundle INSTANCE = GWT.create(IconBundle.class);

    static ImageResource iconForField(FieldType type) {
        return type.accept(new FieldTypeVisitor<ImageResource>() {
            @Override
            public ImageResource visitAttachment(AttachmentType attachmentType) {
                return INSTANCE.textField();
            }

            @Override
            public ImageResource visitCalculated(CalculatedFieldType calculatedFieldType) {
                return INSTANCE.calculatedField();
            }

            @Override
            public ImageResource visitReference(ReferenceType referenceType) {
                return INSTANCE.referenceField();
            }

            @Override
            public ImageResource visitNarrative(NarrativeType narrativeType) {
                return INSTANCE.textField();
            }

            @Override
            public ImageResource visitBoolean(BooleanType booleanType) {
                return INSTANCE.quantityField();
            }

            @Override
            public ImageResource visitQuantity(QuantityType type) {
                return INSTANCE.quantityField();
            }

            @Override
            public ImageResource visitGeoPoint(GeoPointType geoPointType) {
                return INSTANCE.textField();
            }

            @Override
            public ImageResource visitGeoArea(GeoAreaType geoAreaType) {
                return INSTANCE.geoAreaField();
            }

            @Override
            public ImageResource visitEnum(EnumType enumType) {
                return INSTANCE.enumField();
            }

            @Override
            public ImageResource visitBarcode(BarcodeType barcodeType) {
                return INSTANCE.textField();
            }

            @Override
            public ImageResource visitSubForm(SubFormReferenceType subFormReferenceType) {
                return INSTANCE.referenceField();
            }

            @Override
            public ImageResource visitLocalDate(LocalDateType localDateType) {
                return INSTANCE.dateField();
            }

            @Override
            public ImageResource visitMonth(MonthType monthType) {
                return INSTANCE.dateField();
            }

            @Override
            public ImageResource visitYear(YearType yearType) {
                return INSTANCE.dateField();
            }

            @Override
            public ImageResource visitFortnight(FortnightType fortnightType) {
                return INSTANCE.dateField();
            }

            @Override
            public ImageResource visitWeek(EpiWeekType epiWeekType) {
                return INSTANCE.dateField();
            }

            @Override
            public ImageResource visitLocalDateInterval(LocalDateIntervalType localDateIntervalType) {
                return INSTANCE.dateField();
            }

            @Override
            public ImageResource visitText(TextType textType) {
                return INSTANCE.textField();
            }

            @Override
            public ImageResource visitSerialNumber(SerialNumberType serialNumberType) {
                return INSTANCE.quantityField();
            }
        });
    }

    @Source("count.png")
    ImageResource count();

    @Source("field-quantity.png")
    ImageResource quantityField();

    @Source("field-calculated.png")
    ImageResource calculatedField();

    @Source("field-geoarea.png")
    ImageResource geoAreaField();

    @Source("field-text.png")
    ImageResource textField();

    @Source("field-enum.png")
    ImageResource enumField();

    @Source("field-reference.png")
    ImageResource referenceField();

    @Source("field-date.png")
    ImageResource dateField();

    @Source("form.png")
    ImageResource form();

}
