package org.activityinfo.model.query;

import org.activityinfo.model.util.HeapsortColumn;

/**
 * Boolean column view backed by a int[] array
 */
public class BooleanColumnView implements ColumnView {

    private int values[];

    public BooleanColumnView() {
    }

    public BooleanColumnView(int[] values) {
        this.values = values;
    }

    @Override
    public ColumnType getType() {
        return ColumnType.BOOLEAN;
    }

    @Override
    public int numRows() {
        return values.length;
    }

    @Override
    public Object get(int row) {
        int v = values[row];
        if(v == ColumnView.NA) {
            return null;
        } else {
            return v != 0;
        }
    }

    @Override
    public double getDouble(int row) {
        int v = values[row];
        if(v == ColumnView.NA) {
            return Double.NaN;
        } else {
            return v;
        }
    }

    @Override
    public String getString(int row) {
        return null;
    }

    @Override
    public int getBoolean(int row) {
        return values[row];
    }

    @Override
    public boolean isMissing(int row) {
        return values[row] == NA;
    }

    @Override
    public ColumnView select(int[] selectedRows) {
        int filteredValues[] = new int[selectedRows.length];
        for (int i = 0; i < filteredValues.length; i++) {
            int selectedRow = selectedRows[i];
            if(selectedRow == -1) {
                filteredValues[i] = NA;
            } else {
                filteredValues[i] = values[selectedRow];
            }

        }
        return new BooleanColumnView(filteredValues);
    }

    @Override
    public int[] order(int[] sortVector, SortModel.Dir direction, int[] range) {
        int numRows = values.length;
        switch(direction) {
            case ASC:
                if (range == null || range.length == numRows) {
                    HeapsortColumn.heapsortAscending(values, sortVector, numRows);
                } else {
                    HeapsortColumn.heapsortAscending(values, sortVector, range.length, range);
                }
                break;
            case DESC:
                if (range == null || range.length == numRows) {
                    HeapsortColumn.heapsortDescending(values, sortVector, numRows);
                } else {
                    HeapsortColumn.heapsortDescending(values, sortVector, range.length, range);
                }
                break;
        }
        return sortVector;
    }
}
