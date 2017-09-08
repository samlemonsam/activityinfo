package org.activityinfo.store.mysql.side;

import com.google.common.collect.Lists;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.store.spi.CursorObserver;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

class QuantityBuffer implements ValueBuffer {
    private String units;
    private Quantity value;
    private List<CursorObserver<FieldValue>> observers = Lists.newArrayList();

    public QuantityBuffer(QuantityType type) {
        this.units = type.getUnits();
    }

    @Override
    public void add(CursorObserver<FieldValue> observer) {
        observers.add(observer);
    }

    @Override
    public void set(ResultSet rs) throws SQLException {
        double doubleValue = rs.getDouble(DOUBLE_VALUE_COLUMN);
        if(rs.wasNull()) {
            value = null;
        } else {
            value = new Quantity(doubleValue);
        }
    }

    @Override
    public void next() {
        for (CursorObserver<FieldValue> observer : observers) {
            observer.onNext(value);
        }
        value = null;
    }

    @Override
    public void done() {
        for (CursorObserver<FieldValue> observer : observers) {
            observer.done();
        }
    }
}
