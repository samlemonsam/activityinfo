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
package org.activityinfo.store.mysql.mapping;

import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

public class QuantityConverter implements FieldValueConverter {

    private String units;

    public QuantityConverter(String units) {
        this.units = units;
    }

    @Override
    public FieldValue toFieldValue(ResultSet rs, int index) throws SQLException {
        double value = rs.getDouble(index);
        if(rs.wasNull()) {
            return null;
        } else {
            return new Quantity(value);
        }
    }

    @Override
    public Collection<Double> toParameters(FieldValue value) {
        Quantity quantityValue = (Quantity) value;
        return Collections.singleton(quantityValue.getValue());
    }
}
