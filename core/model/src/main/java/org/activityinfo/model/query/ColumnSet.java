package org.activityinfo.model.query;

import com.google.common.base.Joiner;
import org.activityinfo.model.query.ColumnView;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Contains the data
 */
public class ColumnSet {

    private final int numRows;
    private final Map<String, ColumnView> columnViews;

    public ColumnSet(int numRows, @Nonnull Map<String, ColumnView> columnViews) {
        this.numRows = numRows;
        this.columnViews = columnViews;
    }

    /**
     * @return the number of rows in this Table
     */
    public int getNumRows() {
        return numRows;
    }


    @Nonnull
    public Map<String, ColumnView> getColumns() {
        return columnViews;
    }

    /**
     *
     * @param columnModelId the {@code id} of the {@code ColumnModel}
     * @return the {@code ColumnView} generated from the given {@code ColumnModel}
     */
    public ColumnView getColumnView(String columnModelId) {
        return columnViews.get(columnModelId);
    }

    @Override
    public String toString() {
        return "TableData{" +
               "numRows=" + numRows +
               ", columnViews=" + Joiner.on("\n").withKeyValueSeparator("=").join(columnViews) +
               '}';
    }
}
