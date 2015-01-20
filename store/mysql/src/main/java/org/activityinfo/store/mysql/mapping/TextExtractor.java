package org.activityinfo.store.mysql.mapping;

import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TextExtractor implements FieldValueExtractor {
    @Override
    public FieldValue extract(ResultSet rs, int index) throws SQLException {
        return TextValue.valueOf(rs.getString(index));
    }
}
