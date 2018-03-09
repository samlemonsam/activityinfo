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
package org.activityinfo.model.formula.functions;

import org.activityinfo.model.formula.diagnostic.InvalidTypeException;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.HasSetFieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.HasStringValue;

import java.util.Set;

public class Casting {
    public static Quantity toQuantity(FieldValue fieldValue) {
        if(fieldValue instanceof Quantity) {
            return (Quantity) fieldValue;
        } else {
            return new Quantity(Double.NaN);
        }
    }

    public static boolean toBoolean(FieldValue value) {
        if(value == BooleanFieldValue.TRUE) {
            return true;
        } else if(value == BooleanFieldValue.FALSE) {
            return false;
        } else {
            throw new InvalidTypeException("Cannot cast [" + value + "] to boolean");
        }
    }

    public static String toString(FieldValue value) {
        if(value instanceof HasStringValue) {
            return ((HasStringValue) value).asString();
        }
        throw new InvalidTypeException("Cannot cast field value of type " + value.getTypeClass() + " to string");
    }

    public static Set<ResourceId> toSet(FieldValue value) {
        if (value instanceof HasSetFieldValue) {
            return ((HasSetFieldValue) value).getResourceIds();
        }else {
            throw new InvalidTypeException("Cannot cast [" + value + "] to Set<ResourceId>");
        }
    }

}
