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
package org.activityinfo.model.formula.functions;

import org.activityinfo.model.query.ColumnView;

public class AndFunction extends BinaryBooleanOperator {

    public static final AndFunction INSTANCE = new AndFunction();

    public static final String NAME = "&&";

    private AndFunction() {
        super(NAME);
    }

    @Override
    public boolean apply(boolean a, boolean b) {
        return a && b;
    }

    @Override
    public int apply(int a, int b) {
        if(a == ColumnView.TRUE && b == ColumnView.TRUE) {
            return ColumnView.TRUE;
        } else if(a == ColumnView.FALSE || b == ColumnView.FALSE) {
            return ColumnView.FALSE;
        } else {
            return ColumnView.NA;
        }
    }
}
