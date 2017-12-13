package org.activityinfo.model.query;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Simple Array of String values
 */
public class StringArrayColumnView implements ColumnView, Serializable {

    private String[] values;

    protected StringArrayColumnView() {
    }

    public StringArrayColumnView(String[] values) {
        this.values = values;
    }

    public StringArrayColumnView(List<String> values) {
        this.values = values.toArray(new String[values.size()]);
    }

    @Override
    public ColumnType getType() {
        return ColumnType.STRING;
    }

    @Override
    public int numRows() {
        return values.length;
    }

    @Override
    public Object get(int row) {
        return values[row];
    }

    @Override
    public double getDouble(int row) {
        return Double.NaN;
    }

    @Override
    public String getString(int row) {
        return values[row];
    }

    @Override
    public int getBoolean(int row) {
        return NA;
    }

    @Override
    public boolean isMissing(int row) {
        return values[row] == null;
    }

    @Override
    public ColumnView select(int[] selectedRows) {
        String[] filteredValues = new String[selectedRows.length];
        for (int i = 0; i < filteredValues.length; i++) {
            int selectedRow = selectedRows[i];
            if(selectedRow != -1) {
                filteredValues[i] = this.values[selectedRow];
            }
        }
        return new StringArrayColumnView(filteredValues);
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }
}
