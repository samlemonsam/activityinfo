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
package org.activityinfo.geoadmin.source;

import com.vividsolutions.jts.geom.Polygonal;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.time.LocalDateType;
import org.opengis.feature.type.PropertyDescriptor;

import java.util.Date;

public enum AttributeTypeAdapters implements AttributeTypeAdapter {

    AREA {
        @Override
        public FieldType createType() {
            return GeoAreaType.INSTANCE;
        }
    },

    QUANTITY {
        @Override
        public FieldType createType() {
            return QuantityType.TYPE_CLASS.createType();
        }
    },
    
    TEXT {
        @Override
        public FieldType createType() {
            return TextType.SIMPLE;
        }
    },
    
    DATE {
        @Override
        public FieldType createType() {
            return LocalDateType.INSTANCE;
        }
    };


    public abstract FieldType createType();


    public static AttributeTypeAdapter of(PropertyDescriptor descriptor) {
        Class<?> type = descriptor.getType().getBinding();
        if (type.equals(String.class)) {
            return TEXT;
        } else if (Polygonal.class.isAssignableFrom(type)) {
            return AREA;
        } else if (Number.class.isAssignableFrom(type)) {
            return QUANTITY;
        } else if (Date.class.isAssignableFrom(type)) {
            return DATE;
        } else {
            throw new IllegalArgumentException(type.getName());
        }
    }

}
