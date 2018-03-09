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

import org.activityinfo.legacy.shared.reports.content.TreeNode;
import org.activityinfo.legacy.shared.reports.model.ReportElement;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseExcelTableRenderer<ElementT extends ReportElement, ColumnT extends TreeNode> extends
        BaseExcelRenderer<ElementT> {

    protected Map<ColumnT, Integer> colIndexMap;
    protected CellStyle colHeaderStyle;
    protected CellStyle leafColHeaderStyle;

    public BaseExcelTableRenderer(Workbook book, ElementT element) {
        super(book, element);
    }

    protected void initColHeaderStyles(ColumnT root) {

        Font font = createBaseFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);

        colHeaderStyle = book.createCellStyle();
        colHeaderStyle.setWrapText(true);
        colHeaderStyle.setFont(font);
        colHeaderStyle.setAlignment(CellStyle.ALIGN_CENTER);

        leafColHeaderStyle = book.createCellStyle();
        leafColHeaderStyle.setWrapText(true);
        leafColHeaderStyle.setFont(font);
        leafColHeaderStyle.setAlignment(CellStyle.ALIGN_RIGHT);
    }

    protected void generateColumnHeaders(int firstCol, ColumnT root) {

        /*
         * Now try building the column headers
         */

        int depth = root.getDepth();
        colIndexMap = new HashMap<ColumnT, Integer>();

        int startLevel = depth == 0 ? 0 : 1;

        for (int level = startLevel; level <= depth; ++level) {

            Row row = sheet.createRow(rowIndex);

            int colIndex = firstCol;

            List<ColumnT> cols = root.getDescendantsAtDepth(level, true);

            for (ColumnT col : cols) {

                if (col == null) {
                    colIndex++;
                } else {
                    Cell cell = row.createCell(colIndex);
                    cell.setCellValue(factory.createRichTextString(col.getLabel()));
                    cell.setCellStyle(col.isLeaf() ? leafColHeaderStyle : colHeaderStyle);

                    int span = col.getLeaves().size();

                    if (span > 1) {
                        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, colIndex, colIndex + span - 1));
                    }
                    if (col.isLeaf()) {
                        colIndexMap.put(col, colIndex);
                    }

                    colIndex += span;
                }
            }

            rowIndex++;
        }

    }

}
