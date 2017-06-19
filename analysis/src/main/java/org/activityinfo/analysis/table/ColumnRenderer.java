package org.activityinfo.analysis.table;


import org.activityinfo.model.query.ColumnSet;

public interface ColumnRenderer<T> {

    T render(int rowIndex);

    void updateColumnSet(ColumnSet columnSet);

}
