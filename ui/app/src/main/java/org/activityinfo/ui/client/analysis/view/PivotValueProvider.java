package org.activityinfo.ui.client.analysis.view;

import com.sencha.gxt.core.client.ValueProvider;
import org.activityinfo.ui.client.analysis.viewModel.PivotTable;

public class PivotValueProvider implements ValueProvider<PivotRow, String> {

    private PivotTable.Node column;

    public PivotValueProvider(PivotTable.Node column) {
        this.column = column;
    }

    @Override
    public String getValue(PivotRow object) {
        return object.getFormattedValue(column);
    }

    @Override
    public void setValue(PivotRow object, String value) {
    }

    @Override
    public String getPath() {
        return column.flattenLabel();
    }
}
