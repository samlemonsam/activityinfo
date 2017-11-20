package org.activityinfo.io.xls;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;

public class XlsColumnStyleFactory {

    private final CellStyle textStyle;
    private final CellStyle dateStyle;
    private final CellStyle coordStyle;

    public XlsColumnStyleFactory(Workbook book) {

        textStyle = book.createCellStyle();

        dateStyle = book.createCellStyle();
        dateStyle.setDataFormat(book.getCreationHelper().createDataFormat().getFormat("yyyy-mm-dd"));

        coordStyle = book.createCellStyle();
        coordStyle.setDataFormat(book.getCreationHelper().createDataFormat().getFormat("0.000000"));

    }

    public CellStyle getTextStyle() {
        return textStyle;
    }

    public CellStyle getDateStyle() {
        return dateStyle;
    }

    public CellStyle getCoordStyle() {
        return coordStyle;
    }
}
