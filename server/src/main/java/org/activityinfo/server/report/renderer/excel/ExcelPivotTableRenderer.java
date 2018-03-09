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
package org.activityinfo.server.report.renderer.excel;

import org.activityinfo.legacy.shared.reports.content.FilterDescription;
import org.activityinfo.legacy.shared.reports.content.PivotTableData;
import org.activityinfo.legacy.shared.reports.model.PivotTableReportElement;
import org.apache.poi.ss.usermodel.*;

import java.util.List;
import java.util.Map.Entry;

public class ExcelPivotTableRenderer implements ExcelRenderer<PivotTableReportElement> {

    @Override
    public void render(Workbook book, PivotTableReportElement element) {

        /* Generate the actual pivot table data */

        final PivotTableData table = element.getContent().getData();

        /* Generate the excel sheet */

        new BaseExcelTableRenderer<PivotTableReportElement, PivotTableData.Axis>(book, element) {

            @Override
            public List<FilterDescription> generateFilterDescriptions() {
                return element.getContent().getFilterDescriptions();
            }

            private CellStyle[] rowHeaderStyles;

            @Override
            public void generate() {

                /* Initialize Cell Styles */

                initColHeaderStyles(table.getRootColumn());
                initRowHeaderStyles();

                /* Generate the column headers */

                generateColumnHeaders(1, table.getRootColumn());

                int headerHeight = rowIndex;

                /* Create the rows */
                // row headers are in column 1
                sheet.setColumnWidth(0, 40 * 256);
                generateRows(table.getRootRow().getChildList(), 0);

                /* Finalize the sheet */

                sheet.setRowSumsBelow(false);
                sheet.createFreezePane(1, headerHeight);

            }

            protected void initRowHeaderStyles() {

                int depth = table.getRootRow().getDepth();
                rowHeaderStyles = new CellStyle[depth];

                for (int i = 0; i != depth; ++i) {

                    CellStyle style = book.createCellStyle();
                    style.setIndention((short) i);
                    style.setWrapText(true);

                    Font font = createBaseFont();
                    if (i + 1 != depth) {
                        /* Has sub headers */
                        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
                    } else {
                        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
                    }
                    style.setFont(font);

                    rowHeaderStyles[i] = style;
                }
            }

            protected void generateRows(List<PivotTableData.Axis> rows, int indent) {

                for (PivotTableData.Axis pivotRow : rows) {

                    Row row = sheet.createRow(rowIndex++);
                    Cell headerCell = row.createCell(0);
                    headerCell.setCellValue(factory.createRichTextString(pivotRow.getLabel()));
                    headerCell.setCellStyle(rowHeaderStyles[indent]);

                    if (pivotRow.isLeaf()) {

                        for (Entry<PivotTableData.Axis, Integer> entry : colIndexMap.entrySet()) {

                            PivotTableData.Cell pivotCell = pivotRow.getCell(entry.getKey());
                            if (pivotCell != null) {

                                Cell cell = row.createCell(entry.getValue());
                                cell.setCellValue(pivotCell.getValue());
                            }
                        }
                    } else {

                        int groupStart = rowIndex;

                        generateRows(pivotRow.getChildList(), indent + 1);

                        int groupEnd = rowIndex;

                        sheet.groupRow(groupStart, groupEnd);
                    }
                }
            }
        };
    }
}
