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

import org.activityinfo.ui.client.component.importDialog.model.source.SourceRow;
import org.activityinfo.ui.client.component.importDialog.model.strategy.FieldImporterColumn;

import java.util.List;

/**
 * Created by alex on 4/4/14.
 */
public class ValidatedRowTable {

    private List<FieldImporterColumn> columns;
    private List<ValidatedRow> rows;

    public ValidatedRowTable(List<FieldImporterColumn> columns, List<ValidatedRow> rows) {
        this.columns = columns;
        this.rows = rows;
    }

    public List<FieldImporterColumn> getColumns() {
        return columns;
    }

    public List<ValidatedRow> getRows() {
        return rows;
    }

    public ValidatedRow getRow(SourceRow sourceRow) {
        for (ValidatedRow row : rows) {
            if (row.getSourceRow().equals(sourceRow)) {
                return row;
            }
        }
        return null;
    }
}
