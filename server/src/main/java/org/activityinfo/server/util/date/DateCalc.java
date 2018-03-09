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
package org.activityinfo.server.util.date;

import java.util.Calendar;
import java.util.Date;

public class DateCalc {

    public static boolean isOnToday(Date date, Date compare) {
        return isOnToday(date, compare.getTime());
    }

    public static boolean isOnToday(Date date, long compare) {
        return daysBeforeMidnight(date, compare) == 0;
    }

    public static boolean isOnYesterday(Date date, Date compare) {
        return isOnYesterday(date, compare.getTime());
    }

    public static boolean isOnYesterday(Date date, long compare) {
        return daysBeforeMidnight(date, compare) == 1;
    }

    public static int daysBeforeMidnight(Date date, Date compare) {
        return daysBeforeMidnight(date, compare.getTime());
    }

    /**
     * Returns the amount of days before midnight (tomorrow morning) of the specified date. If the specified date to
     * compare with is after midnight, 0 is returned.
     *
     * @param date
     * @param compare
     * @return
     * @throws IllegalArgumentException if the specified date to compare with is after the first date
     */
    public static int daysBeforeMidnight(Date date, long compare) {
        long diff = nextMidnightMillis(date) - compare;
        if (diff < 0) {
            throw new IllegalArgumentException("the date (millis) to compare to should be before the specified date!");
        }
        return (int) (diff / (24 * 60 * 60 * 1000));
    }

    public static long nextMidnightMillis(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, 1);
        setMidnight(c);
        return c.getTimeInMillis();
    }

    public static int absoluteDaysBetween(Date date, Date compare) {
        return absoluteDaysBetween(date, compare.getTime());
    }

    public static int absoluteDaysBetween(Date date, long compare) {
        long diff = date.getTime() - compare;
        return (int) (diff / (24 * 60 * 60 * 1000));
    }

    public static Date daysAgo(Date date, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        if (days > 0) {
            c.add(Calendar.DATE, -days);
        }
        return c.getTime();
    }

    /**
     * Resets the time component of the specified calendar to zero: midnight at the <b>start</b> of the day.
     *
     * @param c
     */
    public static void setMidnight(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    public static Calendar getMidnight(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        setMidnight(c);
        return c;
    }

    public static int quarter(Calendar c) {
        int month = c.get(Calendar.MONTH);
        return month / 3 + 1;
    }

    public static Calendar getMonthStart(Date date) {
        Calendar c = getMidnight(date);
        c.set(Calendar.DAY_OF_MONTH, 1);
        return c;
    }

    public static Calendar getQuarterStart(Date date) {
        Calendar c = getMidnight(date);
        setQuarterStart(c);
        return c;
    }

    public static void setQuarterStart(Calendar c) {
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.MONTH, (quarter(c) - 1) * 3);
    }

    public static Calendar getYearStart(Date date) {
        Calendar c = getMidnight(date);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.MONTH, Calendar.JANUARY);
        return c;
    }
}
