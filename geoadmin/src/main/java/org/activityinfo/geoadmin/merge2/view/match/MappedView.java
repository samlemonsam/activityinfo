package org.activityinfo.geoadmin.merge2.view.match;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;

import java.util.Date;


public abstract class MappedView implements ColumnView {
    
    private final ColumnView source;

    protected MappedView(ColumnView source) {
        this.source = source;
    }

    protected abstract int transformRow(int row);


    @Override
    public final ColumnType getType() {
        return source.getType();
    }

    @Override
    public final Object get(int row) {
        int newRow = transformRow(row);
        if(newRow == -1) {
            return null;
        }
        return source.get(newRow);
    }

    @Override
    public final double getDouble(int row) {
        int newRow = transformRow(row);
        if(newRow == -1) {
            return Double.NaN;
        }
        return source.getDouble(newRow);
    }

    @Override
    public final String getString(int row) {
        int newRow = transformRow(row);
        if(newRow == -1) {
            return null;
        }
        return source.getString(newRow);
    }

    @Override
    public final Date getDate(int row) {
        int newRow = transformRow(row);
        if(newRow == -1) {
            return null;
        }
        return source.getDate(newRow);
    }

    @Override
    public final int getBoolean(int row) {
        int newRow = transformRow(row);
        if(newRow == -1) {
            throw new UnsupportedOperationException("null boolean");
        }
        return source.getBoolean(newRow);
    }
}
