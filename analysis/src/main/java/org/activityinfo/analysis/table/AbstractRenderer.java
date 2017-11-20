package org.activityinfo.analysis.table;

import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;

public abstract class AbstractRenderer<T> implements ColumnRenderer<T> {

    private final String columnId;
    private ColumnView view;

    protected AbstractRenderer(String columnId) {
        this.columnId = columnId;
    }

    @Override
    public final T render(int rowIndex) {
        if(view == null) {
            return null;
        }
        return renderRow(view, rowIndex);
    }

    protected abstract T renderRow(ColumnView view, int rowIndex);

    @Override
    public final void updateColumnSet(ColumnSet columnSet) {
        this.view = columnSet.getColumnView(columnId);
        assert this.view != null : "missing column " + columnId;
    }
}
