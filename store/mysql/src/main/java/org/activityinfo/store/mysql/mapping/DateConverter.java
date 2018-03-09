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
import org.activityinfo.model.type.time.LocalDate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;

import static java.util.Collections.singleton;

/**
 * Converts to/from a {@code LocalDate} and a datetime MySQL column
 */ 
public class DateConverter implements FieldValueConverter {
    
    public static final DateConverter INSTANCE = new DateConverter();
    
    private DateConverter() {
    }
    
    @Override
    public FieldValue toFieldValue(ResultSet rs, int index) throws SQLException {
        java.sql.Date date = rs.getDate(index);
        if(date == null) {
            return null;
        }
        return new LocalDate(date);
    }

    @Override
    public Collection<Date> toParameters(FieldValue value) {
        LocalDate dateValue = (LocalDate) value;
        return singleton(dateValue.atMidnightInMyTimezone());
    }
}
