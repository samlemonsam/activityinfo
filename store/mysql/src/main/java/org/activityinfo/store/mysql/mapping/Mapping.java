package org.activityinfo.store.mysql.mapping;


import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.time.LocalDate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;

import static java.util.Collections.singleton;

public enum Mapping implements FieldValueMapping {
    
    TEXT {
        @Override
        public FieldValue extract(ResultSet rs, int index) throws SQLException {
            return TextValue.valueOf(rs.getString(index));
        }

        @Override
        public Collection<String> toParameters(FieldValue value) {
            return singleton(((TextValue) value).asString());
        }
    },
    DATE {
        @Override
        public FieldValue extract(ResultSet rs, int index) throws SQLException {
            return new LocalDate(rs.getDate(index));
        }

        @Override
        public Collection<Date> toParameters(FieldValue value) {
            LocalDate dateValue = (LocalDate) value;
            return singleton(dateValue.atMidnightInMyTimezone());
        }
    }
}
