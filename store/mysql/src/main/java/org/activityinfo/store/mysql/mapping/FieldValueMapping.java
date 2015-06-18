package org.activityinfo.store.mysql.mapping;

import org.activityinfo.model.type.FieldValue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Extracts a FieldValue from a ResultSet
 */
public interface FieldValueMapping {

    FieldValue extract(ResultSet rs, int index) throws SQLException;
    
    Collection<?> toParameters(FieldValue value);
}
