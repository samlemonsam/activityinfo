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
package org.activityinfo.model.type.geo;

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.number.QuantityType;

/**
 * A value type describing a point within the WGS84 Geographic Reference System.
 */
public class GeoPointType implements RecordFieldType {

    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";

    public static final String TYPE_ID = "geopoint";

    public static final GeoPointType INSTANCE = new GeoPointType();

    public static final FieldTypeClass TYPE_CLASS = new RecordFieldTypeClass() {
        @Override
        public String getId() {
            return TYPE_ID;
        }

        @Override
        public FieldType createType() {
            return INSTANCE;
        }

    };

    private GeoPointType() {  }

    @Override
    public FieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public FieldValue parseJsonValue(JsonValue value) {
        JsonValue object = (JsonValue) value;
        GeoPoint point = new GeoPoint(
                object.get("latitude").asNumber(),
                object.get("longitude").asNumber());
        
        return point;
    }

    @Override
    public <T> T accept(FieldTypeVisitor<T> visitor) {
        return visitor.visitGeoPoint(this);
    }

    @Override
    public boolean isUpdatable() {
        return true;
    }

    /**
     *
     * @return the singleton instance for this type
     */
    private Object readResolve() {
        return INSTANCE;
    }


    @Override
    public FormClass getFormClass() {
        FormClass formClass = new FormClass(ResourceId.valueOf("geoPoint"));
        formClass.setLabel(I18N.CONSTANTS.geographicCoordinatesFieldLabel());
        formClass.addField(ResourceId.valueOf(LATITUDE))
                .setCode(LATITUDE)
                .setLabel(I18N.CONSTANTS.latitude())
                .setType(new QuantityType("degrees"))
                .setRequired(true);
        formClass.addField(ResourceId.valueOf(LONGITUDE))
                .setCode(LONGITUDE)
                .setLabel(I18N.CONSTANTS.longitude())
                .setType(new QuantityType("degrees"))
                .setRequired(true);
        
        return formClass;
    }
}
