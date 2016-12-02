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

import static org.activityinfo.model.type.period.Iso8601.*;

/**
 * @author yuriyz on 01/27/2015.
 */
public class PeriodParser {

    private boolean metTime = false;
    private boolean invalid = false;

    public PeriodParser() {
    }

    public PeriodValue parse(String periodAsIso8601) {
        String originalString = periodAsIso8601;
        if (!Strings.isNullOrEmpty(periodAsIso8601) && periodAsIso8601.startsWith(PERIOD_DESIGNATOR)) {
            periodAsIso8601 = periodAsIso8601.substring(1); // cut off 'P'
            PeriodValue value = new PeriodValue();

            while (!Strings.isNullOrEmpty(periodAsIso8601)) {
                periodAsIso8601 = modify(periodAsIso8601, value);
                if (invalid) {
                    break;
                }
            }
            if (!value.isZero()) {
                return value;
            }
        }
        throw new RuntimeException("Period format is invalid, please correct it according to ISO 8601 specification, period: " + originalString);
    }


    private String modify(String periodAsIso8601, PeriodValue value) {
        if (periodAsIso8601.startsWith(TIME_DESIGNATOR)) {// cut off 'T'
            metTime = true;
            return periodAsIso8601.substring(1);
        }

        String currentNumber = "";
        for (int i = 0; i < periodAsIso8601.length(); i++) {
            char charAt = periodAsIso8601.charAt(i);
            if (Character.isDigit(charAt)) {
                currentNumber = currentNumber + charAt;
                continue;
            }
            String c = charAt + "";
            if (c.equals(YEAR_DESIGNATOR)) {
                if (metTime) {
                    invalid = true;
                    return periodAsIso8601;
                } else {
                    value.setYear(Integer.parseInt(currentNumber));
                    return periodAsIso8601.substring(i + 1);
                }
            } else if (c.equals(MONTH_DESIGNATOR)) {
                if (metTime) {
                    invalid = true;
                    return periodAsIso8601;
                } else {
                    value.setMonth(Integer.parseInt(currentNumber));
                    return periodAsIso8601.substring(i + 1);
                }
            } else if (c.equals(WEEK_DESIGNATOR)) {
                if (metTime) {
                    invalid = true;
                    return periodAsIso8601;
                } else {
                    value.setWeek(Integer.parseInt(currentNumber));
                    return periodAsIso8601.substring(i + 1);
                }
            } else if (c.equals(DAY_DESIGNATOR)) {
                if (metTime) {
                    invalid = true;
                    return periodAsIso8601;
                } else {
                    value.setDay(Integer.parseInt(currentNumber));
                    return periodAsIso8601.substring(i + 1);
                }
            } else if (c.equals(HOUR_DESIGNATOR)) {
                if (metTime) {
                    value.setHour(Integer.parseInt(currentNumber));
                    return periodAsIso8601.substring(i + 1);
                } else {
                    invalid = true;
                    return periodAsIso8601;
                }
            } else if (c.equals(MINUTE_DESIGNATOR)) {
                if (metTime) {
                    value.setMinute(Integer.parseInt(currentNumber));
                    return periodAsIso8601.substring(i + 1);
                } else {
                    invalid = true;
                    return periodAsIso8601;
                }
            } else if (c.equals(SECOND_DESIGNATOR)) {
                if (metTime) {
                    value.setSecond(Integer.parseInt(currentNumber));
                    return periodAsIso8601.substring(i + 1);
                } else {
                    invalid = true;
                    return periodAsIso8601;
                }
            }
        }
        return periodAsIso8601;
    }
}
