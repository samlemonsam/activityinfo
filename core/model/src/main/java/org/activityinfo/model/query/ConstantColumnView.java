package org.activityinfo.model.query;

import com.google.common.base.Strings;
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
    private boolean missing;

    protected ConstantColumnView() {
    }

    public ConstantColumnView(int numRows, double doubleValue) {
        this.type = ColumnType.NUMBER;
        this.doubleValue = doubleValue;
        this.stringValue = null;
        this.booleanValue = (doubleValue != 0) ? TRUE : FALSE;
        this.missing = Double.isNaN(doubleValue);
        this.numRows = numRows;
    }

    public ConstantColumnView(int numRows, String value) {
        this.type = ColumnType.STRING;
        this.doubleValue = Double.NaN;
        this.stringValue = value;
        this.booleanValue = NA;
        this.missing = Strings.isNullOrEmpty(value);
        this.numRows = numRows;
    }

    public ConstantColumnView(int numRows, boolean value) {
        this.type = ColumnType.BOOLEAN;
        this.doubleValue = (value ? 1 : 0);
        this.stringValue = null;
        this.booleanValue = value ? TRUE : FALSE;
        this.numRows = numRows;
        this.missing = false;
    }
    
    public static ConstantColumnView nullBoolean(int numRows) {
        ConstantColumnView view = new ConstantColumnView();
        view.type = ColumnType.BOOLEAN;
        view.doubleValue = Double.NaN;
        view.stringValue = null;
        view.booleanValue = ColumnView.NA;
        view.numRows = numRows;
        view.missing = true;
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
        switch (type) {
            case BOOLEAN:
                return getBoolean(row);
            case NUMBER:
                return getDouble(row);
            case STRING:
                return getString(row);
        }
        throw new IllegalStateException("type: " + type);
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
    public boolean isMissing(int row) {
        return missing;
    }

    @Override
    public ColumnView select(int[] selectedRows) {
        if(missing) {
            return new EmptyColumnView(this.type, selectedRows.length);
        } else {
            if(noneMissing(selectedRows)) {
                return copyOf(selectedRows.length);
            }
        }
        return new FilteredColumnView(this, selectedRows);
    }

    private boolean noneMissing(int[] selectedRows) {
        for (int i = 0; i < selectedRows.length; i++) {
            if(selectedRows[i] == -1) {
                return false;
            }
        }
        return true;
    }

    private ConstantColumnView copyOf(int length) {
        ConstantColumnView copy = new ConstantColumnView();
        copy.numRows = length;
        copy.type = type;
        copy.doubleValue = this.doubleValue;
        copy.stringValue = this.stringValue;
        copy.booleanValue = this.booleanValue;
        copy.missing = this.missing;
        return copy;
    }

    @Override
    public String toString() {
        return "[ " + get(0) + " x " + numRows + " rows ]";
    }
}
