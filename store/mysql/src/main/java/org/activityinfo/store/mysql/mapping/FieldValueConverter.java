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

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Strategy for mapping a {@code FieldValue} to one or more MySQL columns
 */
public interface FieldValueConverter extends Serializable {

    /**
     * Reads and converts column values from a {@code RecordSet} into a {@code FieldValue}
     * @param rs the {@code ResultSet from which to read}
     * @param index the index of the first column in the 
     * @return
     * @throws SQLException
     */
    FieldValue toFieldValue(ResultSet rs, int index) throws SQLException;

    /**
     * Converts a {@code FieldValue} to one or more parameters used in an SQL update or insert statement. 
     * @param value the {@code FieldValue} to convert
     * @return one or more SQL parameters
     */
    Collection<?> toParameters(FieldValue value);
}
