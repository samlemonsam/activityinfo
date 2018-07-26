package org.activityinfo.store.hrd.columns;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.IntOrder;
import org.activityinfo.model.query.SortDir;
import org.activityinfo.model.util.HeapsortColumn;

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
        return new IntValueArrayView(IntValueArray.select(values, rows));
    }

    @Override
    public int[] order(int[] sortVector, SortDir direction, int[] range) {

        IntOrder order = HeapsortColumn.withIntDirection(direction);

        int numRows = values.length;
        if (range == null || range.length == numRows) {
            HeapsortColumn.heapsortInt(values, sortVector, numRows, order);
        } else {
            HeapsortColumn.heapsortInt(values, sortVector, range.length, range, order);
        }
        return sortVector;
    }


}
