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
package org.activityinfo.model.formula.functions.date;

import org.activityinfo.model.type.time.LocalDate;

/**
 * Returns the month of year of the given date.
 */
public class MonthFunction extends DateComponentFunction {

    public static final MonthFunction INSTANCE = new MonthFunction();

    private MonthFunction() {}

    @Override
    public String getId() {
        return "month";
    }

    @Override
    public String getLabel() {
        return "Month";
    }

    @Override
    protected String getUnits() {
        return "months";
    }

    @Override
    protected int apply(LocalDate date) {
        return date.getMonthOfYear();
    }

    public static int fromIsoString(String string) {
        return Integer.parseInt(string.substring(5, 7));
    }
}
