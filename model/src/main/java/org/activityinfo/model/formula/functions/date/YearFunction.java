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


public class YearFunction extends DateComponentFunction {

    public static final YearFunction INSTANCE = new YearFunction();

    private YearFunction() {}

    @Override
    public String getId() {
        return "year";
    }

    @Override
    public String getLabel() {
        return "YEAR";
    }

    @Override
    protected String getUnits() {
        return "years";
    }

    @Override
    protected int apply(LocalDate date) {
        return date.getYear();
    }

}
