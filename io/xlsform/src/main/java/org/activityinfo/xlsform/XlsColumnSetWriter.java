package org.activityinfo.xlsform;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.activityinfo.model.formTree.ColumnNode;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by yuriyz on 9/2/2016.
 */
public class XlsColumnSetWriter {

    private static final short FONT_SIZE = 8;
    private static final short TITLE_FONT_SIZE = 12;

    private static final int HEADER_CELL_HEIGHT = 75;

    private static final int CHARACTERS_PER_WIDTH_UNIT = 256;

    private final HSSFWorkbook book = new HSSFWorkbook();

    private CellStyle titleStyle;
    private CellStyle textStyle;
    private CellStyle headerStyle;

    public XlsColumnSetWriter() {
        declareStyles();
    }

    public XlsColumnSetWriter addSheet(FormTree formTree, ColumnSet columnSet) {
        HSSFSheet sheet = book.createSheet(formTree.getRootFormClass().getLabel());

        writeColumnSet(sheet, formTree, columnSet);

        return this;
    }

    private void writeColumnSet(HSSFSheet sheet, FormTree formTree, ColumnSet columnSet) {
        List<ColumnNode> columnNodes = getColumnNodesWithView(formTree, columnSet);

        writeColumnSetHeader(sheet, columnNodes, formTree.getRootFormClass().getLabel());
        writeColumnSetData(sheet, columnSet, columnNodes);
    }

    private List<ColumnNode> getColumnNodesWithView(FormTree formTree, ColumnSet columnSet) {
        List<ColumnNode> columnNodes = Lists.newArrayList();

        for (ColumnNode column : formTree.getColumnNodes()) {
            if (columnSet.getColumnView(column.getNode().getFieldId().asString()) != null) {
                columnNodes.add(column);
            }
        }
        return columnNodes;
    }

    private void writeColumnSetHeader(HSSFSheet sheet, List<ColumnNode> columnNodes, String title) {
        Cell titleCell = sheet.createRow(0).createCell(0);
        titleCell.setCellValue(book.getCreationHelper().createRichTextString(title));
        titleCell.setCellStyle(titleStyle);

        Row columnHeaderRow = sheet.createRow(1);
        columnHeaderRow.setHeightInPoints(HEADER_CELL_HEIGHT);

        for (ColumnNode column : columnNodes) {
            Cell cell = columnHeaderRow.createCell(columnNodes.indexOf(column));
            cell.setCellStyle(headerStyle);
            cell.setCellValue(column.getHeader());
            sheet.setColumnWidth(columnNodes.indexOf(column), width(column.getHeader()));
        }
    }

    private void writeColumnSetData(HSSFSheet sheet, ColumnSet columnSet, List<ColumnNode> columnNodes) {
        for (int row = 0; row < columnSet.getNumRows(); row++) {

            Row sheetRow = sheet.createRow((row + 2));

            for (ColumnNode key : columnNodes) {
                ColumnView view = columnSet.getColumns().get(key.getNode().getFieldId().asString());
                Object value = view != null ? view.get(row) : null;

                Cell cell = sheetRow.createCell(columnNodes.indexOf(key));
                cell.setCellValue(key.getValueAsString(value));
                cell.setCellStyle(cellStyle());
            }
        }
    }

    private CellStyle cellStyle() {
        return textStyle;
    }

    public void write(OutputStream outputStream) throws IOException {
        book.write(outputStream);
    }

    private static int width(String label) {
        int width = 16 * CHARACTERS_PER_WIDTH_UNIT;

        if (!Strings.isNullOrEmpty(label)) {
            int length = label.length();
            if (length > 40) {
                length = 40;
            }
            if (length > 16) {
                width = length * CHARACTERS_PER_WIDTH_UNIT;
            }
        }
        return width;
    }

    private void declareStyles() {
        textStyle = book.createCellStyle();

        Font headerFont = book.createFont();
        headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

        Font smallFont = book.createFont();
        smallFont.setFontHeightInPoints(FONT_SIZE);

        Font titleFont = book.createFont();
        titleFont.setFontHeightInPoints(TITLE_FONT_SIZE);
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

        titleStyle = book.createCellStyle();
        titleStyle.setFont(titleFont);

        headerStyle = book.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setWrapText(true);
    }
}
