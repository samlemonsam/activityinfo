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

public class QuarterFunction extends DateComponentFunction {

    public static final QuarterFunction INSTANCE = new QuarterFunction();

    private QuarterFunction() {}

    @Override
    public String getId() {
        return "quarter";
    }

    @Override
    public String getLabel() {
        return "quarter";
    }

    @Override
    protected String getUnits() {
        return "quarters";
    }

    @Override
    protected int apply(LocalDate date) {
        return fromMonth(date.getMonthOfYear());
    }

    public static int fromMonth(int month) {
        assert month >= 1 && month <= 12;
        return 1 + ( (month-1) / 3);
    }
}
