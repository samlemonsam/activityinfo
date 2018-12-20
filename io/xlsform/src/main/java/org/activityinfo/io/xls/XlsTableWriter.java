/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.io.xls;

import com.google.common.base.Strings;
import org.activityinfo.analysis.table.EffectiveTableColumn;
import org.activityinfo.analysis.table.EffectiveTableModel;
import org.activityinfo.model.query.ColumnSet;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class XlsTableWriter {

    public static final String EXCEL_MIME_TYPE = "application/vnd.ms-excel";

    private static final short FONT_SIZE = 8;
    private static final short TITLE_FONT_SIZE = 12;

    private static final int HEADER_CELL_HEIGHT = 75;

    private static final int CHARACTERS_PER_WIDTH_UNIT = 256;

    private final HSSFWorkbook book = new HSSFWorkbook();

    private CellStyle titleStyle;
    private CellStyle headerStyle;

    private SheetNamer sheetNamer = new SheetNamer();

    public XlsTableWriter() {
        declareStyles();
    }

    public XlsTableWriter addSheet(EffectiveTableModel tableModel, ColumnSet columnSet) {
        HSSFSheet sheet = book.createSheet(sheetNamer.name(tableModel.getTitle()));

        writeColumnSet(sheet, tableModel, columnSet);

        return this;
    }

    private void writeColumnSet(HSSFSheet sheet, EffectiveTableModel tableModel, ColumnSet columnSet) {

        XlsColumnStyleFactory styleFactory = new XlsColumnStyleFactory(book);

        XlsColumnFactory factory = new XlsColumnFactory(styleFactory, columnSet);
        List<XlsColumn> excelColumns = new ArrayList<>();
        for (EffectiveTableColumn tableColumn : tableModel.getColumns()) {
            excelColumns.addAll(tableColumn.accept(factory));
        }

        writeHeaders(sheet, tableModel.getTitle(), excelColumns);
        writeData(sheet, excelColumns, columnSet.getNumRows());
    }

    private void writeHeaders(HSSFSheet sheet, String title, List<XlsColumn> columns) {
        Cell titleCell = sheet.createRow(0).createCell(0);
        titleCell.setCellValue(book.getCreationHelper().createRichTextString(title));
        titleCell.setCellStyle(titleStyle);

        Row columnHeaderRow = sheet.createRow(1);
        columnHeaderRow.setHeightInPoints(HEADER_CELL_HEIGHT);

        int columnIndex = 0;
        for (int i = 0; i < columns.size(); i++) {
            XlsColumn column = columns.get(i);
            Cell cell = columnHeaderRow.createCell(columnIndex);
            cell.setCellStyle(headerStyle);
            cell.setCellValue(column.getHeading());
            sheet.setColumnWidth(columnIndex, width(column.getHeading()));
            columnIndex++;
        }
    }

    private void writeData(HSSFSheet sheet, List<XlsColumn> columns, int numRows) {
        for (int row = 0; row < numRows; row++) {

            Row sheetRow = sheet.createRow((row + 2));
            for (int i = 0; i < columns.size(); i++) {
                XlsColumn column = columns.get(i);
                if (!column.isMissing(row)) {
                    Cell cell = sheetRow.createCell(i);
                    cell.setCellStyle(column.getStyle());

                    column.setValue(cell, row);
                }
            }
        }
    }

    public void write(OutputStream outputStream) throws IOException {
        book.write(outputStream);
    }

    public HSSFWorkbook getBook() {
        return book;
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

        Font headerFont = book.createFont();
        headerFont.setBold(true);

        Font smallFont = book.createFont();
        smallFont.setFontHeightInPoints(FONT_SIZE);

        Font titleFont = book.createFont();
        titleFont.setFontHeightInPoints(TITLE_FONT_SIZE);
        titleFont.setBold(true);

        titleStyle = book.createCellStyle();
        titleStyle.setFont(titleFont);

        headerStyle = book.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setWrapText(true);
    }
}
