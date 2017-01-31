package org.activityinfo.ui.client.component.importDialog.model.source;

/**
 * Generic interface to a row of an imported table
 */
public interface SourceRow {


    int getRowIndex();

    String getColumnValue(int columnIndex);

    /**
     * @return true if the given column index has no value, is null, a zero-length string, or
     * any other source-specific condition in which a non-zero length string cannot be provided.
     */
    boolean isColumnValueMissing(int columnIndex);

}
