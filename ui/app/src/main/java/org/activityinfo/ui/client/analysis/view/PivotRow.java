package org.activityinfo.ui.client.analysis.view;

import org.activityinfo.ui.client.analysis.viewModel.PivotTable;
import org.activityinfo.ui.client.analysis.viewModel.Point;

public class PivotRow {
    private final String[] rowHeaders;
    private final PivotTable.Node node;

    public PivotRow(String[] rowHeaders, PivotTable.Node node) {

        this.rowHeaders = rowHeaders;
        this.node = node;
    }

    public String getRowHeader(int rowDimensionIndex) {
        return rowHeaders[rowDimensionIndex];
    }

    public String getFormattedValue(PivotTable.Node leafColumn) {
        Point cell = node.getPoint(leafColumn);
        if(cell == null) {
            return null;
        }
        return cell.getFormattedValue();
    }
}
