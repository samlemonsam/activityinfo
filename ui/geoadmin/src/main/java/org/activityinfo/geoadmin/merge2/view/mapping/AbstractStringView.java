package org.activityinfo.geoadmin.merge2.view.mapping;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.FilteredColumnView;
import org.activityinfo.model.query.SortModel;

public abstract class AbstractStringView implements ColumnView {

    @Override
    public final ColumnType getType() {
        return ColumnType.STRING;
    }

    @Override
    public final Object get(int row) {
        return getString(row);
    }

    @Override
    public final double getDouble(int row) {
        return Double.NaN;
    }

    @Override
    public final int getBoolean(int row) {
        return -1;
    }

    @Override
    public final boolean isMissing(int row) {
        return getString(row) == null;
    }

    @Override
    public ColumnView select(int[] selectedRows) {
        return new FilteredColumnView(this, selectedRows);
    }

    @Override
    public int[] order(int[] sortVector, SortModel.Dir direction, int[] range) {
        return sortVector;
    }
}