package org.activityinfo.store.hrd.columns;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.SortModel;

public class IntValueArrayView implements ColumnView {

    private int[] values;

    public IntValueArrayView(int[] values) {
        this.values = values;
    }

    @Override
    public ColumnType getType() {
        return ColumnType.NUMBER;
    }

    @Override
    public int numRows() {
        return values.length;
    }

    @Override
    public Object get(int row) {
        return getDouble(row);
    }

    @Override
    public double getDouble(int row) {
        int value = values[row];
        if(value == IntValueArray.MISSING) {
            return Double.NaN;
        } else {
            return value;
        }
    }

    @Override
    public String getString(int row) {
        return null;
    }

    @Override
    public boolean isMissing(int row) {
        return values[row] == IntValueArray.MISSING;
    }

    @Override
    public ColumnView select(int[] rows) {
        int[] selected = new int[rows.length];
        for (int i = 0; i < rows.length; i++) {
            int row = rows[i];
            if(row == -1) {
                selected[i] = IntValueArray.MISSING;
            } else {
                selected[i] = values[row];
            }
        }
        return new IntValueArrayView(selected);
    }

    @Override
    public int[] order(int[] sortVector, SortModel.Dir direction, int[] range) {
        throw new UnsupportedOperationException("TODO");
    }
}
