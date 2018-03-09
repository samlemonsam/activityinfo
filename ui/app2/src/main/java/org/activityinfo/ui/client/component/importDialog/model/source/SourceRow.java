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
package org.activityinfo.ui.client.component.importDialog.model.source;

/**
 * Generic interface to a row of an imported table
 */
public interface SourceRow {


    int getRowIndex();

    String getColumnValue(int columnIndex);

    /**
     * @return true if the given column index has no value, is null, a zero-length string, or
     * any other source-specific condition in which a non-zero length string cannot be provided.
     */
    boolean isColumnValueMissing(int columnIndex);

}
