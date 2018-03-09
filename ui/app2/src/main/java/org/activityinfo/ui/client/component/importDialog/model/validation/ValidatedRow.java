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
package org.activityinfo.ui.client.component.importDialog.model.validation;

import com.google.common.collect.Lists;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceRow;

import java.util.List;

public class ValidatedRow {
    private SourceRow row;
    private List<ValidationResult> columns = Lists.newArrayList();

    public ValidatedRow(SourceRow row, List<ValidationResult> columns) {
        this.row = row;
        this.columns = columns;
    }

    public ValidationResult getResult(int columnIndex) {
        return columns.get(columnIndex);
    }

    public SourceRow getSourceRow() {
        return row;
    }

    /**
     * Row is valid if all column's results are valid, otherwise invalid.
     *
     * @return whether row is valid
     */
    public boolean isValid() {
        for (ValidationResult column : columns) {
            if (column.getState() == ValidationResult.State.ERROR && !column.hasReferenceMatch()) {
                return false;
            }
        }
        return true;
    }
}
