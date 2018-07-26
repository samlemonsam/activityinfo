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
package org.activityinfo.client;

import com.google.common.base.Preconditions;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.FilteredColumnView;
import org.activityinfo.model.query.SortDir;


public class ColumnViewWrapper implements ColumnView {

    @SuppressWarnings("GwtInconsistentSerializableClass")
    private JsonValue array;
    private int numRows;

    public ColumnViewWrapper() {
    }

    public ColumnViewWrapper(int numRows, JsonValue array) {
        this.array = array;
        this.numRows = numRows;
    }

    @Override
    public ColumnType getType() {
        return ColumnType.STRING;
    }

    @Override
    public int numRows() {
        return numRows;
    }

    @Override
    public Object get(int row) {
        return getString(row);
    }

    @Override
    public double getDouble(int row) {
        JsonValue jsonElement = array.get(row);
        if(jsonElement.isJsonNull()) {
            return Double.NaN;
        } else {
            return jsonElement.asNumber();
        }
    }

    @Override
    public String getString(int row) {
        Preconditions.checkPositionIndex(row, numRows);

        JsonValue jsonElement = array.get(row);
        if(jsonElement.isJsonNull()) {
            return null;
        } else {
            return jsonElement.asString();
        }
    }

    @Override
    public int getBoolean(int row) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMissing(int row) {
        return array.get(row).isJsonNull();
    }

    @Override
    public ColumnView select(int[] selectedRows) {
        return new FilteredColumnView(this, selectedRows);
    }


    @Override
    public String toString() {
        return array.toJson();
    }

    @Override
    public int[] order(int[] sortVector, SortDir direction, int[] range) {
        // TODO: ColumnViewWrapper Sorting
        // Do not sort on column
        return sortVector;
    }
}
