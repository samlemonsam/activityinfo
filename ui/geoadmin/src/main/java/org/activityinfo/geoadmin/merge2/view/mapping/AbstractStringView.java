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
package org.activityinfo.geoadmin.merge2.view.mapping;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.FilteredColumnView;
import org.activityinfo.model.query.SortDir;

public abstract class AbstractStringView implements ColumnView {

    @Override
    public final ColumnType getType() {
        return ColumnType.STRING;
    }

    @Override
    public final Object get(int row) {
        return getString(row);
    }

    @Override
    public final double getDouble(int row) {
        return Double.NaN;
    }

    @Override
    public final int getBoolean(int row) {
        return -1;
    }

    @Override
    public final boolean isMissing(int row) {
        return getString(row) == null;
    }

    @Override
    public ColumnView select(int[] selectedRows) {
        return new FilteredColumnView(this, selectedRows);
    }

    @Override
    public int[] order(int[] sortVector, SortDir direction, int[] range) {
        throw new UnsupportedOperationException();
    }
}