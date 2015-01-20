package org.activityinfo.store.mysql.mapping;


import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.time.LocalDate;

import java.sql.ResultSet;
import java.sql.SQLException;

public enum Extractor implements FieldValueExtractor {
    
    TEXT {
        @Override
        public FieldValue extract(ResultSet rs, int index) throws SQLException {
            return TextValue.valueOf(rs.getString(index));
        }
    },
    DATE {
        @Override
        public FieldValue extract(ResultSet rs, int index) throws SQLException {
            return new LocalDate(rs.getDate(index));
        }
    }
}
