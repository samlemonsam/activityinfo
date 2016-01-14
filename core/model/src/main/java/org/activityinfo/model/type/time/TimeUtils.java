package org.activityinfo.model.type.time;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.gwt.core.shared.GWT;

import java.util.GregorianCalendar;

/**
 * Encapsulates time/date computations for which we need to provide
 * GWT emulations
 */
class TimeUtils {

    static LocalDate getLastDayOfMonth(MonthValue month) {
        if(GWT.isClient()) {
            int zeroBasedMonthOfYear = month.getMonthOfYear() - 1;
            int lastDay = lastDay(month.getYear(), zeroBasedMonthOfYear);
            return new LocalDate(month.getYear(), month.getMonthOfYear(), lastDay);

        } else {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.set(month.getYear(), month.getMonthOfYear() - 1, 1);

            return new LocalDate(month.getYear(), month.getMonthOfYear(),
                    calendar.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
        }
    }

    private static native int lastDay(int year, int zeroBasedMonth) /*-{
        var d = new Date(year, zeroBasedMonth + 1, 0);
        return d.getDay();
    }-*/;

}
