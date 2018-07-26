package org.activityinfo.store.hrd.columns;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.IntOrder;
import org.activityinfo.model.query.SortDir;
import org.activityinfo.model.util.HeapsortColumn;

import java.util.function.IntFunction;

public class DateColumnView implements ColumnView {

    private int[] values;
    private IntFunction<String> renderer;

    public DateColumnView(int[] values, IntFunction<String> renderer) {
        this.values = values;
        this.renderer = renderer;
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
        return getString(row);
    }

    @Override
    public double getDouble(int row) {
        return Double.NaN;
    }

    @Override
    public String getString(int row) {
        int encoded = values[row];
        if(encoded == IntValueArray.MISSING) {
            return null;
        } else {
            return renderer.apply(encoded);
        }
    }

    @Override
    public boolean isMissing(int row) {
        return values[row] == IntValueArray.MISSING;
    }

    @Override
    public ColumnView select(int[] rows) {
        return new DateColumnView(IntValueArray.select(values, rows), renderer);
    }

    @Override
    public int[] order(int[] sortVector, SortDir direction, int[] range) {

        // We can use natural order, as we have chosen an encoding that preserves
        // the order of dates under transformation

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
