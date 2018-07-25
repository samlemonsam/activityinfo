package org.activityinfo.store.hrd.columns;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.SortModel;

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
    public int[] order(int[] sortVector, SortModel.Dir direction, int[] range) {
        throw new UnsupportedOperationException("TODO");
    }
}
