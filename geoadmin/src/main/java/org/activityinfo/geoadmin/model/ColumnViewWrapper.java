package org.activityinfo.geoadmin.model;

import com.google.gson.JsonArray;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;

import java.util.Date;


public class ColumnViewWrapper implements ColumnView {
    private JsonArray array;
    private final int numRows;

    public ColumnViewWrapper(int numRows, JsonArray array) {
        this.array = array;
        this.numRows = numRows;
    }

    @Override
    public ColumnType getType() {
        return ColumnType.STRING;
    }

    @Override
    public int numRows() {
        return numRows;
    }

    @Override
    public Object get(int row) {
        return getString(row);
    }

    @Override
    public double getDouble(int row) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getString(int row) {
        return array.get(row).getAsString();
    }

    @Override
    public Date getDate(int row) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getBoolean(int row) {
        throw new UnsupportedOperationException();
    }


    @Override
    public String toString() {
        return array.toString();
    }
}
