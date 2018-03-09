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


public class DayFunction extends DateComponentFunction {

    public static final DayFunction INSTANCE = new DayFunction();

    private DayFunction() {}

    @Override
    public String getId() {
        return "day";
    }

    @Override
    public String getLabel() {
        return "day";
    }

    @Override
    protected String getUnits() {
        return "days";
    }

    @Override
    protected int apply(LocalDate date) {
        return date.getDayOfMonth();
    }
}
