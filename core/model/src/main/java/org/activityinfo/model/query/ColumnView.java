package org.activityinfo.model.query;

import org.activityinfo.model.type.geo.Extents;

import java.io.Serializable;

public interface ColumnView extends Serializable {

    int TRUE = 1;
    int FALSE = 0;
    int NA = Integer.MAX_VALUE;


    ColumnType getType();

    int numRows();

    Object get(int row);

    double getDouble(int row);

    String getString(int row);

    Extents getExtents(int row);

    /**
     *
     * @param row
     * @return ColumnView#TRUE, ColumnView#FALSE, or ColumnView#NA if the value is not null or missing
     */
    int getBoolean(int row);
}
