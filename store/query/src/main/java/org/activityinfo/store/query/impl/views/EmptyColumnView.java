package org.activityinfo.store.query.impl.views;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;

import java.io.Serializable;
import java.util.Date;

public class EmptyColumnView implements ColumnView, Serializable {

    private ColumnType type;
    private int rowCount;

    protected EmptyColumnView() {
    }

    public EmptyColumnView(int rowCount, ColumnType type) {
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
    public Date getDate(int row) {
        return null;
    }

    @Override
    public int getBoolean(int row) {
        return NA;
    }

    @Override
    public String toString() {
        return "[ " + numRows() + " empty values ]";
    }
}
