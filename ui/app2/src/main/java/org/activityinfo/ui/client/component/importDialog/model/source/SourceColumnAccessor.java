package org.activityinfo.ui.client.component.importDialog.model.source;

import org.activityinfo.ui.client.component.importDialog.model.strategy.ColumnAccessor;

public class SourceColumnAccessor implements ColumnAccessor {

    private final SourceColumn column;
    private final int columnIndex;

    public SourceColumnAccessor(SourceColumn column) {
        this.column = column;
        this.columnIndex = this.column.getIndex();
    }

    @Override
    public String getHeading() {
        return column.getHeader();
    }

    @Override
    public String getValue(SourceRow row) {
        return row.getColumnValue(columnIndex);
    }

    @Override
    public boolean isMissing(SourceRow row) {
        String value = row.getColumnValue(columnIndex);
        return value == null || value.trim().length() == 0; // trim before check on length
    }

    @Override
    public String toString() {
        return column.getHeader();
    }
}
