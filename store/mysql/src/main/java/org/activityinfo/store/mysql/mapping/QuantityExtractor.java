package org.activityinfo.store.mysql.mapping;

import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class QuantityExtractor implements FieldValueExtractor {

    private String units;

    public QuantityExtractor(String units) {
        this.units = units;
    }

    @Override
    public FieldValue extract(ResultSet rs, int index) throws SQLException {
        double value = rs.getDouble(index);
        if(rs.wasNull()) {
            return null;
        } else {
            return new Quantity(value, units);
        }
    }
}
