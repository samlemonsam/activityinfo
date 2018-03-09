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
package org.activityinfo.server.report;

import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.reports.content.EntityCategory;
import org.activityinfo.legacy.shared.reports.content.FilterDescription;
import org.activityinfo.legacy.shared.reports.content.PivotContent;
import org.activityinfo.legacy.shared.reports.content.PivotTableData;
import org.activityinfo.legacy.shared.reports.model.AdminDimension;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.legacy.shared.reports.model.PivotTableReportElement;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex Bertram (akbertram@gmail.com)
 */
public class DummyPivotTableData2 {

    public Dimension partnerDim = new Dimension(DimensionType.Partner);
    public Dimension provinceDim = new AdminDimension(1);
    public List<Dimension> rowDims = new ArrayList<Dimension>();
    public List<Dimension> colDims = new ArrayList<Dimension>();

    public PivotTableData.Axis[] leafRows = new PivotTableData.Axis[4];
    public PivotTableData table = new PivotTableData();
    public PivotTableData.Axis row1 = table.getRootRow().addChild(partnerDim,
            new EntityCategory(1, "AVSI"), "AVSI", null);
    public PivotTableData.Axis row2 = table.getRootRow().addChild(partnerDim,
            new EntityCategory(1, "NRC"), "NRC", null);

    public DummyPivotTableData2() {

        rowDims.add(partnerDim);
        rowDims.add(provinceDim);

        leafRows[0] = row1.addChild(provinceDim, new EntityCategory(61,
                "Nord Kivu"), "Nord", null);
        leafRows[1] = row1.addChild(provinceDim, new EntityCategory(62,
                "Sud Kivu"), "Sud Kivu", null);

        leafRows[2] = row2.addChild(provinceDim, new EntityCategory(61,
                "Nord Kivu"), "Nord", null);
        leafRows[3] = row2.addChild(provinceDim, new EntityCategory(62,
                "Sud Kivu"), "Sud Kivu", null);

        for (int i = 0; i != leafRows.length; ++i) {

            leafRows[i].setValue(table.getRootColumn(),
                    (double) ((i + 1) * 100));

        }

    }

    public PivotTableReportElement testElement() {
        PivotTableReportElement element = new PivotTableReportElement();
        element.setTitle("Foobar 1612");
        element.setRowDimensions(rowDims);
        element.setColumnDimensions(colDims);
        element.setContent(new PivotContent(table,
                new ArrayList<FilterDescription>()));

        return element;
    }

}