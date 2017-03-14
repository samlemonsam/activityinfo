package org.activityinfo.ui.client.analysis.viewModel;

import org.activityinfo.model.query.ColumnView;

public class ColumnReader implements DimensionReader {

    private final ColumnView columnView;

    public ColumnReader(ColumnView columnView) {
        this.columnView = columnView;
    }

    @Override
    public String read(int row) {
        return columnView.getString(row);
    }
}
