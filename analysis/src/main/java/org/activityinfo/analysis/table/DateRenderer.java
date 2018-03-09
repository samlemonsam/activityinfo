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
import org.activityinfo.model.type.time.LocalDate;

import java.util.Date;

public class DateRenderer implements ColumnRenderer<Date> {

    private final String id;
    private ColumnView view = null;

    public DateRenderer(String id) {
        this.id = id;
    }

    @Override
    public Date render(int rowIndex) {
        assert view != null : "updateColumnSet() has not been called";
        String text = view.getString(rowIndex);
        if(text == null) {
            return null;
        }
        return LocalDate.parse(text).atMidnightInMyTimezone();
    }

    @Override
    public void updateColumnSet(ColumnSet columnSet) {
        view = columnSet.getColumnView(id);
    }
}
