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
package org.activityinfo.ui.client.component.importDialog.mapping;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.view.client.SelectionModel;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceColumn;

/**
 * Header for a SourceColumn
 */
class GridHeader extends Header<SourceColumn> {
    private SourceColumn column;
    private SelectionModel<SourceColumn> columnSelectionModel;

    public GridHeader(SourceColumn column, GridHeaderCell cell,
                      SelectionModel<SourceColumn> columnSelectionModel) {
        super(cell);
        this.column = column;
        this.columnSelectionModel = columnSelectionModel;
    }

    @Override
    public SourceColumn getValue() {
        return column;
    }

    @Override
    public void onBrowserEvent(Cell.Context context, Element elem, NativeEvent event) {
        columnSelectionModel.setSelected(column, true);
    }


}
