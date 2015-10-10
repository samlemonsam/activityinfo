package org.activityinfo.store.mysql.mapping;

import org.activityinfo.model.type.FieldValue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Strategy for mapping a {@code FieldValue} to one or more MySQL columns
 */
public interface FieldValueConverter {

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
