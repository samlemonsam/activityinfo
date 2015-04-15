package org.activityinfo.store.query.impl.views;

import com.google.common.base.Strings;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;

import java.io.Serializable;
import java.util.Date;

public class ConstantColumnView implements ColumnView, Serializable {

    private ColumnType type;
    private double doubleValue;
    private String stringValue;
    private boolean booleanValue;
    private int numRows;

    protected ConstantColumnView() {
    }

    public ConstantColumnView(int numRows, double doubleValue) {
        this.type = ColumnType.STRING;
        this.doubleValue = doubleValue;
        this.stringValue = null;
        this.booleanValue = (doubleValue != 0);
        this.numRows = numRows;
    }

    public ConstantColumnView(int numRows, String value) {
        this.type = ColumnType.STRING;
        this.doubleValue = Double.NaN;
        this.stringValue = value;
        this.booleanValue = Strings.isNullOrEmpty(value);
        this.numRows = numRows;
    }

    public ConstantColumnView(int numRows, boolean value) {
        this.type = ColumnType.BOOLEAN;
        this.doubleValue = (value ? 1 : 0);
        this.stringValue = null;
        this.booleanValue = value;
        this.numRows = numRows;
    }

    public static ConstantColumnView create(int numRows, Object value) {
        if(value instanceof Number) {
            return new ConstantColumnView(numRows, ((Number) value).doubleValue());

        } else if(value instanceof String || value == null) {
            return new ConstantColumnView(numRows, (String)value);

        } else if(value instanceof Boolean) {
            return new ConstantColumnView(numRows, value == Boolean.TRUE);

        } else {
            throw new IllegalArgumentException("value: " + value);
        }
    }

    @Override
    public ColumnType getType() {
        return type;
    }

    @Override
    public int numRows() {
        return numRows;
    }

    @Override
    public Object get(int row) {
        return stringValue;
    }

    @Override
    public double getDouble(int row) {
        return doubleValue;
    }

    @Override
    public String getString(int row) {
       return stringValue;
    }

    @Override
    public Date getDate(int row) {
        return null;
    }

    @Override
    public int getBoolean(int row) {
        return booleanValue ? 1 : 0;
    }

    @Override
    public String toString() {
        return "[ " + get(0) + " x " + numRows + " rows ]";
    }
}
