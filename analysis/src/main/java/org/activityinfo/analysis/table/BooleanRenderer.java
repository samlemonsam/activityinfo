package org.activityinfo.analysis.table;

import org.activityinfo.model.query.ColumnView;

class BooleanRenderer extends AbstractRenderer<Boolean> {


    protected BooleanRenderer(String columnId) {
        super(columnId);
    }

    @Override
    protected Boolean renderRow(ColumnView view, int rowIndex) {
        return null;
    }


}
