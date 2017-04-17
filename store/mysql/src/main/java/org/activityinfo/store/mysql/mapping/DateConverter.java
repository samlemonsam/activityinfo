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
