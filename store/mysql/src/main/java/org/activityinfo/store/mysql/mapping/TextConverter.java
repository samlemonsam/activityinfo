package org.activityinfo.store.mysql.mapping;

import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import static java.util.Collections.singleton;

/**
 * Converts to/from a {@code TextValue} and character MySQL column
 */
public class TextConverter implements FieldValueConverter {
    
    public static final TextConverter INSTANCE = new TextConverter();
    
    private TextConverter() {}
    
    @Override
    public FieldValue toFieldValue(ResultSet rs, int index) throws SQLException {
        return TextValue.valueOf(rs.getString(index));
    }

    @Override
    public Collection<String> toParameters(FieldValue value) {
        return singleton(((TextValue) value).asString());
    }
}
