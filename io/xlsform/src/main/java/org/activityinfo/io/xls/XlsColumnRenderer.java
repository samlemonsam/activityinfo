package org.activityinfo.io.xls;

import org.apache.poi.ss.usermodel.Cell;

/**
 * Represents a single column
 */
public interface XlsColumnRenderer {



    boolean isMissing(int row);

    /**
     * Sets the cell's value to this column's value at {@code rowIndex}
     */
    void setValue(Cell cell, int row);

}
