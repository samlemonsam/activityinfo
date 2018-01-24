package org.activityinfo.model.query;

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

    /**
     *
     * @param row
     * @return ColumnView#TRUE, ColumnView#FALSE, or ColumnView#NA if the value is not null or missing
     */
    int getBoolean(int row);

    boolean isMissing(int row);

    /**
     * Creates a new ColumnView with the rows listed in {@code rows}.
     *
     * @param rows an array of rows indexes into this ColumnView
     * @return a new ColumnView containing the selected rows.
     */
    ColumnView select(int[] rows);

    /**
     * Order the column rows in the given {@code direction},
     * within the given row {@code range}
     *
     * @param sortVector int array of sorted row indices
     * @param direction of ordering
     * @param range of rows to order
     * @return int array of the reordered row index
     */
    int[] order(int[] sortVector, SortModel.Dir direction, int[] range);

}
