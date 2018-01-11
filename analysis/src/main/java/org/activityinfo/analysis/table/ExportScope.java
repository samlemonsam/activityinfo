package org.activityinfo.analysis.table;

public enum ExportScope {
    /**
     * All columns or rows are exported, disregarding the user's current selection
     */
    ALL,

    /**
     * Only the columns or rows that the user has selected will be exported.
     */
    SELECTED
}
