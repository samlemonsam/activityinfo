package org.activityinfo.store.query.impl.views;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

public class DoubleArrayColumnView implements ColumnView, Serializable {
    private double[] values;

    protected DoubleArrayColumnView() {
    }

    public DoubleArrayColumnView(double[] values) {
        this.values = values;
    }

    @Override
    public ColumnType getType() {
        return ColumnType.NUMBER;
    }

    @Override
    public int numRows() {
        return values.length;
    }

    @Override
    public Object get(int row) {
        double value = values[row];
        if(Double.isNaN(value)) {
            return null;
        } else {
            return value;
        }
    }

    @Override
    public double getDouble(int row) {
        return values[row];
    }

    @Override
    public String getString(int row) {
        return null;
    }

    @Override
    public Date getDate(int row) {
        return null;
    }

    @Override
    public int getBoolean(int row) {
        double x = getDouble(row);
        if(Double.isNaN(x)) {
            return NA;
        } else if(x == 0) {
            return FALSE;
        } else {
            return TRUE;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(int i=0;i!=Math.min(10, values.length);++i) {
            if(i > 0) {
                sb.append(", ");
            }
            if(Double.isNaN(values[i])) {
                sb.append("NaN");
            } else {
                sb.append(values[i]);
            }
        }
        if(numRows() > 10) {
            sb.append("... total rows = ").append(numRows());
        }
        sb.append(" ]");
        return sb.toString();
    }
}
