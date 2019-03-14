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
package org.activityinfo.ui.client.widget.form;

/**
 * @author yuriyz on 11/10/2014.
 */
public class GridCol {

    public static final String COL_XS = "col-xs";

    public static final int MAX_COLUMN_COUNT = 12;
    public static final String DEFAULT_COL = COL_XS;

    public static String col(int width) {
        return DEFAULT_COL + "-" + width;
    }

    public static String remainingCol(int width) {
        return DEFAULT_COL + "-" + (MAX_COLUMN_COUNT - width);
    }

}
