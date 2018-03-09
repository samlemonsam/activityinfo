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

import java.io.Serializable;

public interface ColumnView extends Serializable {

    int TRUE = 1;
    int FALSE = 0;
    int NA = Integer.MAX_VALUE;


    ColumnType getType();

    int numRows();

    Object get(int row);

    double getDouble(int row);

    String getString(int row);

    /**
     *
     * @param row
     * @return ColumnView#TRUE, ColumnView#FALSE, or ColumnView#NA if the value is not null or missing
     */
    int getBoolean(int row);

    boolean isMissing(int row);

    /**
     * Creates a new ColumnView with the rows listed in {@code rows}.
     *
     * @param rows an array of rows indexes into this ColumnView
     * @return a new ColumnView containing the selected rows.
     */
    ColumnView select(int[] rows);

    /**
     * Order the column rows in the given {@code direction},
     * within the given row {@code range}
     *
     * @param sortVector int array of sorted row indices
     * @param direction of ordering
     * @param range of rows to order
     * @return int array of the reordered row index
     */
    int[] order(int[] sortVector, SortModel.Dir direction, int[] range);

}
