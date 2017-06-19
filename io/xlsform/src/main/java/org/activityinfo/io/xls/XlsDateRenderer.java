package org.activityinfo.io.xls;

import org.activityinfo.analysis.table.ColumnRenderer;
import org.apache.poi.ss.usermodel.Cell;

import java.util.Date;

public class XlsDateRenderer implements XlsColumnRenderer {

    private ColumnRenderer<Date> renderer;

    public XlsDateRenderer(ColumnRenderer<Date> renderer) {
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
