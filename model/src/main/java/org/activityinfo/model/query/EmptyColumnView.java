package org.activityinfo.model.query;

import java.io.Serializable;

public class EmptyColumnView implements ColumnView, Serializable {

    private ColumnType type;
    private int rowCount;

    protected EmptyColumnView() {
    }

    public EmptyColumnView(ColumnType type, int rowCount) {
        this.type = type;
        this.rowCount = rowCount;
    }

    @Override
    public ColumnType getType() {
        return type;
    }

    @Override
    public int numRows() {
        return rowCount;
    }

    @Override
    public Object get(int row) {
        return null;
    }

    @Override
    public double getDouble(int row) {
        return Double.NaN;
    }

    @Override
    public String getString(int row) {
        return null;
    }

    @Override
    public int getBoolean(int row) {
        return NA;
    }

    @Override
    public boolean isMissing(int row) {
        return true;
    }

    @Override
    public ColumnView select(int[] selectedRows) {
        return new EmptyColumnView(this.type, selectedRows.length);
    }

    @Override
    public String toString() {
        return "[ " + numRows() + " empty values ]";
    }
}
