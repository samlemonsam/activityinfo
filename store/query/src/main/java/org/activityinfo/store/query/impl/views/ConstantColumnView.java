package org.activityinfo.store.query.impl.views;

import com.google.common.base.Strings;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.geo.Extents;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.TextValue;

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

    public static ConstantColumnView create(int numRows, FieldValue value) {
        if(value == null) {
            return new ConstantColumnView(numRows, (String)null);
            
        } else if(value instanceof Quantity) {
            return new ConstantColumnView(numRows, ((Quantity) value).getValue());

        } else if(value instanceof TextValue) {
            return new ConstantColumnView(numRows, ((TextValue) value).asString());

        } else if(value instanceof BooleanFieldValue) {
            return new ConstantColumnView(numRows, value == BooleanFieldValue.TRUE);
            
        } else {
            throw new IllegalArgumentException("value: " + value + " [" + value.getClass().getName() + "]");
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
    public Extents getExtents(int row) {
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
