package org.activityinfo.geoadmin.merge2.view.mapping;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.geo.Extents;

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
    public Extents getExtents(int row) {
        return null;
    }

    @Override
    public final int getBoolean(int row) {
        return -1;
    }
}
