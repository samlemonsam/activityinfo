package org.activityinfo.store.mysql.mapping;

import org.activityinfo.model.type.FieldValue;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Extracts a FieldValue from a ResultSet
 */
public interface FieldValueExtractor {

    FieldValue extract(ResultSet rs, int index) throws SQLException;
}
