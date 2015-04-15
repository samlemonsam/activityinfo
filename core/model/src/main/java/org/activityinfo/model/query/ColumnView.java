package org.activityinfo.model.query;

import java.util.Date;

public interface ColumnView {

    public static final int TRUE = 1;
    public static final int FALSE = 0;
    public static final int NA = Integer.MAX_VALUE;


    ColumnType getType();

    int numRows();

    Object get(int row);

    double getDouble(int row);

    String getString(int row);

    Date getDate(int row);

    /**
     *
     * @param row
     * @return ColumnView#TRUE, ColumnView#FALSE, or ColumnView#NA if the value is not null or missing
     */
    int getBoolean(int row);
}
