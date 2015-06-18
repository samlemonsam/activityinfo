package org.activityinfo.store.mysql.mapping;

import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

public class QuantityMapping implements FieldValueMapping {

    private String units;

    public QuantityMapping(String units) {
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

    @Override
    public Collection<Double> toParameters(FieldValue value) {
        Quantity quantityValue = (Quantity) value;
        return Collections.singleton(quantityValue.getValue());
    }
}
