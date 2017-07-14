package org.activityinfo.store.query.impl.join;

public interface ForeignKey {

    int numRows();

    ForeignKey filter(int[] selectedRows);

    String getKey(int rowIndex);

    int[] buildMapping(PrimaryKeyMap pk);
}
