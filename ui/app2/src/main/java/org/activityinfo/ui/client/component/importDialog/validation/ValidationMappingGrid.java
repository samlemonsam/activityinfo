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

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.ui.ResizeComposite;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.component.importDialog.model.validation.ValidationResult;
import org.activityinfo.ui.client.style.table.DataGridResources;

import java.util.List;

/**
 * @author yuriyz on 4/30/14.
 */
public class ValidationMappingGrid extends ResizeComposite {

    private DataGrid<ValidationResult> dataGrid;

    public ValidationMappingGrid() {
        this.dataGrid = new DataGrid<>(100, DataGridResources.INSTANCE);
        dataGrid.addColumn(new ValidationClassGridColumn(), new TextHeader(I18N.CONSTANTS.message()));
        initWidget(dataGrid);
    }

    public void refresh(List<ValidationResult> resultList) {
        dataGrid.setRowData(resultList);
    }
}
