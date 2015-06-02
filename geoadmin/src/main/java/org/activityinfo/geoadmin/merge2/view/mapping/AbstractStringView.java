package org.activityinfo.geoadmin.merge2.view.mapping;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;

import java.util.Date;

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
    public final Date getDate(int row) {
        return null;
    }

    @Override
    public final int getBoolean(int row) {
        return -1;
    }
}
