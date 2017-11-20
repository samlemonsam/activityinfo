package org.activityinfo.analysis.table;

import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;

public class DoubleRenderer implements ColumnRenderer<Double> {

    private String id;
    private ColumnView view;

    DoubleRenderer(String id) {
        this.id = id;
    }

    @Override
    public Double render(int rowIndex) {
        double value = view.getDouble(rowIndex);
        if (Double.isNaN(value)) {
            return null;
        }
        return value;
    }

    @Override
    public void updateColumnSet(ColumnSet columnSet) {
        this.view = columnSet.getColumnView(id);
    }
}
