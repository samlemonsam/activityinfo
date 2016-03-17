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
        return new LocalDate(rs.getDate(index));
    }

    @Override
    public Collection<Date> toParameters(FieldValue value) {
        LocalDate dateValue = (LocalDate) value;
        return singleton(dateValue.atMidnightInMyTimezone());
    }
}
