package org.activityinfo.analysis.table;

import org.activityinfo.model.query.ColumnView;

class StringRenderer extends AbstractRenderer<String> {

    protected StringRenderer(String columnId) {
        super(columnId);
    }

    @Override
    protected String renderRow(ColumnView view, int rowIndex) {
        return view.getString(rowIndex);
    }
}
