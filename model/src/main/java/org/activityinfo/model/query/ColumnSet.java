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
package org.activityinfo.model.query;

import com.google.common.base.Joiner;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Contains the data
 */
public class ColumnSet {

    private final int numRows;
    private final Map<String, ColumnView> columnViews;

    public ColumnSet(int numRows, @Nonnull Map<String, ColumnView> columnViews) {
        this.numRows = numRows;
        this.columnViews = columnViews;
    }

    /**
     * @return the number of rows in this Table
     */
    public int getNumRows() {
        return numRows;
    }


    @Nonnull
    public Map<String, ColumnView> getColumns() {
        return columnViews;
    }

    /**
     *
     * @param columnModelId the {@code id} of the {@code ColumnModel}
     * @return the {@code ColumnView} generated from the given {@code ColumnModel}
     */
    public ColumnView getColumnView(String columnModelId) {
        return columnViews.get(columnModelId);
    }

    @Override
    public String toString() {
        return "TableData{" +
               "numRows=" + numRows +
               ", columnViews=" + Joiner.on("\n").withKeyValueSeparator("=").join(columnViews) +
               '}';
    }
}
