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
package org.activityinfo.analysis.table;

import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;

public abstract class AbstractRenderer<T> implements ColumnRenderer<T> {

    private final String columnId;
    private ColumnView view;

    protected AbstractRenderer(String columnId) {
        this.columnId = columnId;
    }

    @Override
    public final T render(int rowIndex) {
        if(view == null) {
            return null;
        }
        return renderRow(view, rowIndex);
    }

    protected abstract T renderRow(ColumnView view, int rowIndex);

    @Override
    public final void updateColumnSet(ColumnSet columnSet) {
        this.view = columnSet.getColumnView(columnId);
        assert this.view != null : "missing column " + columnId;
    }
}
