package org.activityinfo.store.query.impl.join;

import org.activityinfo.store.query.shared.join.PrimaryKeyMap;

public interface ForeignKey {

    int numRows();

    ForeignKey filter(int[] selectedRows);

    String getKey(int rowIndex);

    int[] buildMapping(PrimaryKeyMap pk);
}
