package org.activityinfo.store.mysql.side;

import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.service.store.CursorObserver;

import java.sql.ResultSet;
import java.sql.SQLException;

class QuantityBuffer implements ValueBuffer {
    private String units;
    private Quantity value;
    private CursorObserver<FieldValue> observer;

    public QuantityBuffer(QuantityType type, CursorObserver<FieldValue> observer) {
        this.units = type.getUnits();
        this.observer = observer;
    }

    @Override
    public void set(ResultSet rs) throws SQLException {
        double doubleValue = rs.getDouble(DOUBLE_VALUE_COLUMN);
        if(rs.wasNull()) {
            value = null;
        } else {
            value = new Quantity(doubleValue, units);
        }
    }

    @Override
    public void next() {
        observer.onNext(value);
        value = null;
    }

    @Override
    public void done() {
        observer.done();
    }
}
