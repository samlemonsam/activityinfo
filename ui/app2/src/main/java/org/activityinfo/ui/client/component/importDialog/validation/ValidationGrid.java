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
package org.activityinfo.ui.client.component.importDialog.validation;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.ui.ResizeComposite;
import org.activityinfo.ui.client.component.importDialog.model.strategy.FieldImporterColumn;
import org.activityinfo.ui.client.component.importDialog.model.validation.ValidatedRow;
import org.activityinfo.ui.client.component.importDialog.model.validation.ValidatedRowTable;
import org.activityinfo.ui.client.style.table.DataGridResources;
import org.activityinfo.ui.client.util.GwtUtil;

/**
 * A second DataGrid that allows the user to the resolve
 * any problems before import
 */
public class ValidationGrid extends ResizeComposite {

    private DataGrid<ValidatedRow> dataGrid;

    public ValidationGrid() {
        this.dataGrid = new DataGrid<>(100, DataGridResources.INSTANCE);
        initWidget(dataGrid);
        this.dataGrid.setWidth("100%");
        this.dataGrid.setHeight("100%");
    }

    public void refresh(ValidatedRowTable table) {
        while (dataGrid.getColumnCount() > 0) {
            dataGrid.removeColumn(0);
        }
        for(int i = 0; i< table.getColumns().size(); i++) {
            final FieldImporterColumn column = table.getColumns().get(i);
            String columnHeader = column.getAccessor().getHeading();
            dataGrid.addColumn(new ValidationRowGridColumn(column.getAccessor(), i),
                    new TextHeader(columnHeader));
            dataGrid.setColumnWidth(i, GwtUtil.columnWidthInEm(columnHeader), Style.Unit.EM);
        }
        dataGrid.setRowData(table.getRows());
    }

    public int getInvalidRowsCount() {
        int invalidRowsCount = 0;
        for (ValidatedRow row : dataGrid.getVisibleItems()) {
            if (!row.isValid()) {
                invalidRowsCount++;
            }
        }
        return invalidRowsCount;
    }
}
