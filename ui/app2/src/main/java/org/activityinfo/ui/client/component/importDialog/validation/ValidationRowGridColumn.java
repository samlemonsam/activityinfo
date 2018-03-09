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

import com.google.gwt.user.cellview.client.Column;
import org.activityinfo.ui.client.component.importDialog.model.strategy.ColumnAccessor;
import org.activityinfo.ui.client.component.importDialog.model.validation.ValidatedRow;
import org.activityinfo.ui.client.component.importDialog.validation.cells.ValidationResultCell;

public class ValidationRowGridColumn extends Column<ValidatedRow, ValidatedRow> {

    /**
     * Construct a new Column with a given {@link com.google.gwt.cell.client.Cell}.
     *
     * @param accessor
     */
    public ValidationRowGridColumn(ColumnAccessor accessor, int columnIndex) {
        super(new ValidationResultCell(accessor, columnIndex));
    }

    @Override
    public ValidatedRow getValue(ValidatedRow row) {
        return row;
    }
}
