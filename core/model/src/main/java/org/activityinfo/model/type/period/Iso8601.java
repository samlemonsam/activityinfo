package org.activityinfo.model.type.period;
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

import com.google.common.base.Strings;
import org.activityinfo.model.type.time.LocalDate;

import java.util.Date;

/**
 * @author yuriyz on 01/27/2015.
 */
public class Iso8601 {

    public static final String PERIOD_DESIGNATOR = "P";
    public static final String YEAR_DESIGNATOR = "Y";
    public static final String MONTH_DESIGNATOR = "M";
    public static final String WEEK_DESIGNATOR = "W";
    public static final String DAY_DESIGNATOR = "D";
    public static final String TIME_DESIGNATOR = "T";
    public static final String HOUR_DESIGNATOR = "H";
    public static final String MINUTE_DESIGNATOR = "M";
    public static final String SECOND_DESIGNATOR = "S";

    private Iso8601() {
    }

    public static PeriodValue parse(String periodAsIso8601) {
        return new PeriodParser().parse(periodAsIso8601);
    }

    public static String asIso8601String(PeriodValue period) {
        if (period == null || period.isZero()) {
            throw new IllegalArgumentException();
        }

        String result = PERIOD_DESIGNATOR;
        if (period.getYear() != 0) {
            result = result + period.getYear() + YEAR_DESIGNATOR;
        }
        if (period.getMonth() != 0) {
            result = result + period.getMonth() + MONTH_DESIGNATOR;
        }
        if (period.getWeek() != 0) {
            result = result + period.getWeek() + WEEK_DESIGNATOR;
        }
        if (period.getDay() != 0) {
            result = result + period.getDay() + DAY_DESIGNATOR;
        }

        String resultTime = "";
        if (period.getHour() != 0) {
            resultTime = resultTime + period.getHour() + HOUR_DESIGNATOR;
        }
        if (period.getMinute() != 0) {
            resultTime = resultTime + period.getMinute() + MINUTE_DESIGNATOR;
        }
        if (period.getSecond() != 0) {
            resultTime = resultTime + period.getSecond() + SECOND_DESIGNATOR;
        }

        if (!Strings.isNullOrEmpty(resultTime)) {
            result = result + TIME_DESIGNATOR + resultTime;
        }

        return result;
    }

    public static String asString(Date date) {
        return asString(new LocalDate(date));
    }

    public static String asString(LocalDate localDate) {
        String monthOfYear = appendZeroIfNeeded(localDate.getMonthOfYear());
        String dayOfMonth = appendZeroIfNeeded(localDate.getDayOfMonth());
        return localDate.getYear() + monthOfYear + dayOfMonth;
    }

    private static String appendZeroIfNeeded(int integer) {
        return appendZeroIfNeeded(Integer.toString(integer));
    }


    private static String appendZeroIfNeeded(String str) {
        if (str.length() == 1) {
            return "0" + str;
        } else if (str.length() == 2) {
            return str;
        }
        throw new RuntimeException("Unable to normalize string: " + str);

    }
}
