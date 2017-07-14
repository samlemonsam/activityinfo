package org.activityinfo.store.query.server.columns;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;

import java.io.Serializable;

public abstract class AbstractNumberColumn implements ColumnView, Serializable {
    @Override
    public final ColumnType getType() {
        return ColumnType.NUMBER;
    }

    @Override
    public final Object get(int row) {
        return getDouble(row);
    }


    @Override
    public final String getString(int row) {
        return null;
    }

    @Override
    public final int getBoolean(int row) {
        double x = getDouble(row);
        if(Double.isNaN(x)) {
            return NA;
        } else if(x == 0) {
            return FALSE;
        } else {
            return TRUE;
        }
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "{numRows=" + numRows() + "}";
    }
}
