package org.activityinfo.store.query.shared.join;

/**
 * Maps the Record's id to its row index in the ColumnSet
 */
public interface PrimaryKeyMap {

    int numRows();

    int getRowIndex(String recordId);

    boolean contains(String recordId);
}
