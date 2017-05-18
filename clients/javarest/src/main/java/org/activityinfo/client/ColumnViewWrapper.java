package org.activityinfo.client;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;


public class ColumnViewWrapper implements ColumnView {

    @SuppressWarnings("GwtInconsistentSerializableClass")
    private JsonArray array;
    private int numRows;

    public ColumnViewWrapper() {
    }

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
        JsonElement jsonElement = array.get(row);
        if(jsonElement.isJsonNull()) {
            return Double.NaN;
        } else {
            return jsonElement.getAsDouble();
        }
    }

    @Override
    public String getString(int row) {
        Preconditions.checkPositionIndex(row, numRows);

        JsonElement jsonElement = array.get(row);
        if(jsonElement.isJsonNull()) {
            return null;
        } else {
            return jsonElement.getAsString();
        }
    }

    @Override
    public int getBoolean(int row) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMissing(int row) {
        return array.get(row).isJsonNull();
    }


    @Override
    public String toString() {
        return array.toString();
    }
}
