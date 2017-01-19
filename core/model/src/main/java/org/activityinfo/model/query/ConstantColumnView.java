package org.activityinfo.model.query;

import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.NullFieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.time.LocalDate;

import java.io.Serializable;

public class ConstantColumnView implements ColumnView, Serializable {

    private ColumnType type;
    private double doubleValue;
    private String stringValue;
    private int booleanValue;
    private int numRows;

    protected ConstantColumnView() {
    }

    public ConstantColumnView(int numRows, double doubleValue) {
        this.type = ColumnType.NUMBER;
        this.doubleValue = doubleValue;
        this.stringValue = null;
        this.booleanValue = (doubleValue != 0) ? TRUE : FALSE;
        this.numRows = numRows;
    }

    public ConstantColumnView(int numRows, String value) {
        this.type = ColumnType.STRING;
        this.doubleValue = Double.NaN;
        this.stringValue = value;
        this.booleanValue = NA;
        this.numRows = numRows;
    }

    public ConstantColumnView(int numRows, boolean value) {
        this.type = ColumnType.BOOLEAN;
        this.doubleValue = (value ? 1 : 0);
        this.stringValue = null;
        this.booleanValue = value ? TRUE : FALSE;
        this.numRows = numRows;
    }
    
    public static ConstantColumnView nullBoolean(int numRows) {
        ConstantColumnView view = new ConstantColumnView();
        view.type = ColumnType.BOOLEAN;
        view.doubleValue = Double.NaN;
        view.stringValue = null;
        view.booleanValue = ColumnView.NA;
        view.numRows = numRows;
        return view;
    }

    public static ConstantColumnView create(int numRows, FieldValue value) {
        if(value == null || value == NullFieldValue.INSTANCE) {
            return new ConstantColumnView(numRows, (String) null);

        } else if(value instanceof Quantity) {
            return new ConstantColumnView(numRows, ((Quantity) value).getValue());

        } else if(value instanceof TextValue) {
            return new ConstantColumnView(numRows, ((TextValue) value).asString());

        } else if(value instanceof BooleanFieldValue) {
            return new ConstantColumnView(numRows, value == BooleanFieldValue.TRUE);

        } else if(value instanceof LocalDate) {
            return new ConstantColumnView(numRows, value.toString());
            
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
    public int getBoolean(int row) {
        return booleanValue;
    }

    @Override
    public String toString() {
        return "[ " + get(0) + " x " + numRows + " rows ]";
    }
}
