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
package org.activityinfo.geoadmin.merge2.view.match;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.FilteredColumnView;
import org.activityinfo.model.query.SortModel;


public abstract class MappedView implements ColumnView {
    
    private final ColumnView source;

    protected MappedView(ColumnView source) {
        this.source = source;
    }

    protected abstract int transformRow(int row);


    @Override
    public final ColumnType getType() {
        return source.getType();
    }

    @Override
    public final Object get(int row) {
        int newRow = transformRow(row);
        if(newRow == -1) {
            return null;
        }
        return source.get(newRow);
    }

    @Override
    public final double getDouble(int row) {
        int newRow = transformRow(row);
        if(newRow == -1) {
            return Double.NaN;
        }
        return source.getDouble(newRow);
    }

    @Override
    public final String getString(int row) {
        int newRow = transformRow(row);
        if(newRow == -1) {
            return null;
        }
        return source.getString(newRow);
    }

    @Override
    public final int getBoolean(int row) {
        int newRow = transformRow(row);
        if(newRow == -1) {
            throw new UnsupportedOperationException("null boolean");
        }
        return source.getBoolean(newRow);
    }

    @Override
    public boolean isMissing(int row) {
        int newRow = transformRow(row);
        if(newRow == -1) {
            return true;
        }
        return source.isMissing(newRow);
    }

    @Override
    public ColumnView select(int[] selectedRows) {
        return new FilteredColumnView(this, selectedRows);
    }

    @Override
    public int[] order(int[] sortVector, SortModel.Dir direction, int[] range) {
        return source.order(sortVector, direction, range);
    }
}
