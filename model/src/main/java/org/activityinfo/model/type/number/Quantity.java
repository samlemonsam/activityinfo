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
package org.activityinfo.model.type.number;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;

public class Quantity implements FieldValue {

    private final double value;

    public Quantity(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return FieldTypeClass.QUANTITY;
    }

    @Override
    public JsonValue toJson() {
        if(Double.isNaN(value)) {
            return Json.createNull();
        } else {
            return Json.create(value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Quantity quantity = (Quantity) o;

        if (Double.compare(quantity.value, value) != 0) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(value);
        return ((int) (temp ^ (temp >>> 32)));
    }

    @Override
    public String toString() {
        return "Quantity{" +
               "value=" + value +
               '}';
    }
}
