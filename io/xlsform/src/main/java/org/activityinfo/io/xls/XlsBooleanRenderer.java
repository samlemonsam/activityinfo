package org.activityinfo.io.xls;

import org.activityinfo.analysis.table.ColumnRenderer;
import org.apache.poi.ss.usermodel.Cell;

public class XlsBooleanRenderer implements XlsColumnRenderer {

    private ColumnRenderer<Boolean> renderer;

    public XlsBooleanRenderer(ColumnRenderer<Boolean> renderer) {
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
