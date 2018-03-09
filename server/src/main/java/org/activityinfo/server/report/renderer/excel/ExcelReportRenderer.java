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

import com.google.inject.Inject;
import org.activityinfo.legacy.shared.reports.model.*;
import org.activityinfo.server.report.renderer.Renderer;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.io.OutputStream;

public class ExcelReportRenderer implements ExcelRenderer<Report>, Renderer {

    private final ExcelPivotTableRenderer pivotTableRenderer;
    private final ExcelTableRenderer tableRenderer;
    private final ExcelChartRenderer chartRenderer;

    @Inject
    public ExcelReportRenderer(ExcelPivotTableRenderer pivotTableRenderer,
                               ExcelTableRenderer tableRenderer,
                               ExcelChartRenderer chartRenderer) {
        this.pivotTableRenderer = pivotTableRenderer;
        this.tableRenderer = tableRenderer;
        this.chartRenderer = chartRenderer;
    }

    public ExcelReportRenderer() {
        this.chartRenderer = new ExcelChartRenderer();
        this.pivotTableRenderer = new ExcelPivotTableRenderer();
        this.tableRenderer = new ExcelTableRenderer();
    }

    @Override
    public void render(ReportElement element, OutputStream os) throws IOException {

        HSSFWorkbook book = new HSSFWorkbook();

        if (element instanceof Report) {
            render(book, (Report) element);
        } else if (element instanceof PivotTableReportElement) {
            pivotTableRenderer.render(book, (PivotTableReportElement) element);

        } else if (element instanceof TableElement) {
            tableRenderer.render(book, (TableElement) element);

        } else if (element instanceof PivotChartReportElement) {
            chartRenderer.render(book, (PivotChartReportElement) element);
        }
        book.write(os);

    }

    @Override
    public String getMimeType() {
        return "application/vnd.ms-excel";
    }

    @Override
    public String getFileSuffix() {
        return ".xls";
    }

    @Override
    public void render(Workbook workbook, Report report) {

        /*
         * Create a worksheet for each report element
         */

        for (ReportElement element : report.getElements()) {

            if (element instanceof PivotTableReportElement) {

                pivotTableRenderer.render(workbook, (PivotTableReportElement) element);

            } else if (element instanceof TableElement) {

                tableRenderer.render(workbook, (TableElement) element);

            }

        }
    }
}
