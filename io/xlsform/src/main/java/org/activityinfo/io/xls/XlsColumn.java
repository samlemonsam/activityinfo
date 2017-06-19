package org.activityinfo.io.xls;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

public class XlsColumn {

    private String heading;
    private XlsColumnRenderer renderer;
    private CellStyle style;

    public XlsColumn(String heading, CellStyle style, XlsColumnRenderer renderer) {
        this.heading = heading;
        this.style = style;
        this.renderer = renderer;
    }


    public String getHeading() {
        return heading;
    }

    public boolean isMissing(int row) {
        return renderer.isMissing(row);
    }

    public CellStyle getStyle() {
        return style;
    }

    public void setValue(Cell cell, int row) {
        renderer.setValue(cell, row);
    }
}
