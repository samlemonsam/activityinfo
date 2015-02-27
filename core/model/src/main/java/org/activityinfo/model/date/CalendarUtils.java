package org.activityinfo.model.date;
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

import com.google.common.base.Preconditions;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import org.activityinfo.model.type.time.LocalDate;

import java.util.Date;

/**
 * The first epi week of the year ends, by definition, on the first Saturday of January,
 * as long as it falls at least four days into the month. Each epi week begins on a Sunday and ends on a Saturday.
 * <p/>
 * http://www.cmmcp.org/epiweek.htm
 * <p/>
 * MMWR week numbering is sequential beginning with 1 and incrementing with each week to a maximum of 52 or 53.
 * MMWR week #1 of an MMWR year is the first week of the year that has at least four days in the calendar year.
 * For example, if January 1 occurs on a Sunday, Monday, Tuesday or Wednesday, the calendar week that includes January 1 would be
 * MMWR week #1. If January 1 occurs on a Thursday, Friday, or Saturday, the calendar week that includes January 1 would be the last
 * MMWR week of the previous year (#52 or #53). Because of this rule, December 29, 30, and 31 could potentially fall into
 * MMWR week #1 of the following MMWR year.
 * <p/>
 * http://wwwn.cdc.gov/nndss/document/MMWR_week_overview.pdf
 *
 * @author yuriyz on 02/26/2015.
 */
public class CalendarUtils {

    // for mock purpose
    public static interface DayOfWeekProvider {
        public DayOfWeek dayOfWeek(Date date);
    }

    public static final DayOfWeekProvider DAY_OF_WEEK_PROVIDER = new DayOfWeekProvider() {
        @Override
        public DayOfWeek dayOfWeek(Date date) {
            return DayOfWeek.dayOfWeek(date);
        }
    };

    private CalendarUtils() {
    }

    public static Date getFirstDateOfYear(Date date) {
        return new LocalDate(date.getYear() + 1900, 1, 1).atMidnightInMyTimezone();
    }

    public static Date getFirstDateOfYear(int year) {
        return new LocalDate(year, 1, 1).atMidnightInMyTimezone();
    }

    public static EpiWeek epiWeek(Date date) {
        return epiWeek(date, DAY_OF_WEEK_PROVIDER);
    }

    public static EpiWeek epiWeek(Date date, DayOfWeekProvider dayOfWeekProvider) {
        Date target = CalendarUtil.copyDate(date);
        CalendarUtil.resetTime(target);

        int dayInMonth = date.getDate();
        int year = date.getYear() + 1900;

        DayOfWeek dayOfWeek = dayOfWeekProvider.dayOfWeek(date);
        Date firstDayOfEpicWeekInYear = firstDayOfEpicWeekInYear(dayOfWeekProvider, year);

        // Number of days between first day of epic week in year and target date
        int daysBetween = CalendarUtil.getDaysBetween(firstDayOfEpicWeekInYear, target);
        if (daysBetween < 0 ||
                (daysBetween == 0 && (dayInMonth < 4) && (dayOfWeek == DayOfWeek.FRIDAY || dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY))) {
            Date lastDayInPreviousYear = new LocalDate(year - 1, 12, 31).atMidnightInMyTimezone();
            EpiWeek epiWeek = epiWeek(lastDayInPreviousYear, dayOfWeekProvider);
            epiWeek.setYear(year - 1);
            return epiWeek;
        }
        // Calculate week number: number of weeks between target date and january 4th
        int weekInYear = (int) (1 + Math.ceil(daysBetween / 7));
        Preconditions.checkState(weekInYear >= 1 && weekInYear <= 53, "Bug! Week number must be between [1..53] but is " + weekInYear);

        if (firstDayOfEpicWeekInYear.getYear() < year && date.getMonth() == 0 &&
                (weekInYear == 1 || weekInYear == 52 || weekInYear == 53)) {

            if (dayOfWeek == DayOfWeek.THURSDAY || dayOfWeek == DayOfWeek.FRIDAY || dayOfWeek == DayOfWeek.SATURDAY) {
                year = firstDayOfEpicWeekInYear.getYear() + 1900;
            }
        }

        // case when date falls into next year week
        if (weekInYear == 52 || weekInYear == 53) {
            if (date.getMonth() == 11 &&  // if december
                    (dayInMonth == 28 || dayInMonth == 29 || dayInMonth == 30 || dayInMonth == 31) &&
                    (dayOfWeek == DayOfWeek.SUNDAY || dayOfWeek == DayOfWeek.MONDAY || dayOfWeek == DayOfWeek.TUESDAY)) {
                return new EpiWeek()
                        .setWeekInYear(1)
                        .setYear(year + 1);
            }
        }
        return new EpiWeek()
                .setWeekInYear(weekInYear)
                .setYear(year);
    }

    public static Date firstDayOfEpicWeekInYear(DayOfWeekProvider dayOfWeekProvider, int year) {
        Preconditions.checkNotNull(dayOfWeekProvider);
        Preconditions.checkState(year > 0, "Year can't be less than zero.");


        Date january4 = new LocalDate(year, 1, 4).atMidnightInMyTimezone();
        DayOfWeek dayOfWeekOfJan4 = dayOfWeekProvider.dayOfWeek(january4);
        switch (dayOfWeekOfJan4) {
            case SUNDAY:
                return january4;
            case MONDAY:
            case TUESDAY:
            case WEDNESDAY:
            case THURSDAY:
            case FRIDAY:
            case SATURDAY:
                CalendarUtil.addDaysToDate(january4, -dayOfWeekOfJan4.getValue());
                return january4;

        }

        throw new RuntimeException("Failed to identify first day of epic week in year: " + year);

    }

    public static DateRange rangeByEpiWeek(EpiWeek epiWeek) {
        return rangeByEpiWeek(DAY_OF_WEEK_PROVIDER, epiWeek);
    }

    public static DateRange rangeByEpiWeekFromDate(Date date) {
        EpiWeek epiWeek = epiWeek(date);
        return rangeByEpiWeek(epiWeek);
    }

    public static DateRange rangeByEpiWeek(DayOfWeekProvider dayOfWeekProvider, EpiWeek epiWeek) {
        Date date = new LocalDate(epiWeek.getYear(), 1, 1).atMidnightInMyTimezone();
        int dayInYearInsideWeek = epiWeek.getWeekInYear() * 7 - 4;
        CalendarUtil.addDaysToDate(date, dayInYearInsideWeek);
        DayOfWeek dateOfWeek = dayOfWeekProvider.dayOfWeek(date);

        Date startDate = CalendarUtil.copyDate(date);
        CalendarUtil.addDaysToDate(startDate, -dateOfWeek.getValue());

        Date endDate = CalendarUtil.copyDate(date);
        CalendarUtil.addDaysToDate(endDate, 6 - dateOfWeek.getValue());

        return new DateRange(startDate, endDate);
    }
}
