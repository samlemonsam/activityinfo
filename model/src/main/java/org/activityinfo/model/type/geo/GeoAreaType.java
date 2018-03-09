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

import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.*;

/**
 * A value type describing a geographic area on the Earth's surface
 * in the WGS84 geographic reference system.
 */
public class GeoAreaType implements FieldType {

    public static final String TYPE_ID = "geoArea";

    public static final GeoAreaType INSTANCE = new GeoAreaType();

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

    private GeoAreaType() {  }

    @Override
    public FieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public FieldValue parseJsonValue(JsonValue value) {
        JsonValue object = value;
        JsonValue bbox = object.get("bbox");
        return new GeoArea(Extents.fromJsonObject(bbox));
    }

    @Override
    public <T> T accept(FieldTypeVisitor<T> visitor) {
        return visitor.visitGeoArea(this);
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
            
}
