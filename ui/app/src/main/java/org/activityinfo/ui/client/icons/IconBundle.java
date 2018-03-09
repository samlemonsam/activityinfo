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
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.time.LocalDateType;

public interface IconBundle extends ClientBundle {

    IconBundle INSTANCE = GWT.create(IconBundle.class);

    static ImageResource iconForField(FieldType type) {
        if(type instanceof QuantityType) {
            return INSTANCE.quantityField();

        } else if(type instanceof TextType) {
            return INSTANCE.textField();

        } else if(type instanceof LocalDateType) {
            return INSTANCE.dateField();

        } else if(type instanceof ReferenceType) {
            return INSTANCE.referenceField();

        } else if(type instanceof EnumType) {
            return INSTANCE.enumField();

        } else if(type instanceof GeoAreaType) {
            return INSTANCE.geoAreaField();

        } else {
            return INSTANCE.calculatedField();
        }
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

    @Source("database-open.png")
    ImageResource databaseOpen();

    @Source("database-closed.png")
    ImageResource databaseClosed();

    @Source("report.png")
    ImageResource report();
}
