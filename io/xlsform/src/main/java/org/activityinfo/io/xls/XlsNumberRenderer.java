package org.activityinfo.io.xls;

import org.activityinfo.analysis.table.ColumnRenderer;
import org.apache.poi.ss.usermodel.Cell;

public class XlsNumberRenderer implements XlsColumnRenderer {

    private final ColumnRenderer<Double> renderer;

    public XlsNumberRenderer(ColumnRenderer<Double> renderer) {
        this.renderer = renderer;
    }

    @Override
    public boolean isMissing(int row) {
        return renderer.render(row) == null;
    }

    @Override
    public void setValue(Cell cell, int row) {
        cell.setCellValue(renderer.render(row));
    }
}
