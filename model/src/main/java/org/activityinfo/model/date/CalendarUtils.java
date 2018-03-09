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
package org.activityinfo.model.date;

import com.bedatadriven.rebar.time.calendar.LocalDate;
import com.google.common.collect.Maps;
import org.activityinfo.model.util.Pair;

import java.util.Date;
import java.util.Map;


public class CalendarUtils {

    private CalendarUtils() {
    }

    /**
     * Compares two date with a precision of one second.
     *
     * @param baseDate   The base date
     * @param beforeDate The date supposed to be before.
     * @return True if the beforeDate is indeed before the baseDate.
     */
    public static boolean before(final Date baseDate, final Date beforeDate) {
        if ((baseDate == null) || (beforeDate == null)) {
            throw new IllegalArgumentException(
                    "Can't compare the dates, at least one of them is null");
        }

        final long baseTime = baseDate.getTime() / 1000;
        final long beforeTime = beforeDate.getTime() / 1000;
        return beforeTime < baseTime;
    }

    public static Map<Pair<Integer, Integer>, LocalDateRange> getLastFourQuarterMap() {
        return getLastFourQuarterMap(new LocalDate());
    }

    public static Map<Pair<Integer, Integer>, LocalDateRange> getLastFourQuarterMap(LocalDate date) {
        int year = date.getYear();
        int quarter = date.getMonthOfYear() / 3;

        Map<Pair<Integer, Integer>, LocalDateRange> result = Maps.newLinkedHashMap();

        for (int i = 0; i < 4; ++i) {
            quarter = quarter - 1;
            if (quarter < 0) {
                year = year - 1;
                quarter = 3;
            }
            result.put(Pair.newPair(year, quarter), createQuarterRange(year, quarter));
        }
        return result;
    }

    public static LocalDateRange createQuarterRange(int year, int quarter) {

        int startMonth = quarter * 3;
        int endMonth = startMonth + 4;


        LocalDate from = new LocalDate(year, startMonth, 1);
        LocalDate to = new LocalDate(year, endMonth,
                org.activityinfo.model.type.time.LocalDate.getLastDayOfMonth(year, endMonth));

        return new LocalDateRange(from, to);
    }
}
