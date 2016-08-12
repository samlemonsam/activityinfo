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

import com.bedatadriven.rebar.time.calendar.LocalDate;
import com.google.common.collect.Lists;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * @author yuriyz on 07/06/2015.
 */
public class CalendarUtilsTest {

    @Test
    public void firstDayOfEpicWeekInYear() {
        assertFirstDayOfEpicWeekInYear(2017, 2017, 1, 1);
        assertFirstDayOfEpicWeekInYear(2016, 2016, 1, 3);
        assertFirstDayOfEpicWeekInYear(2015, 2015, 1, 4);
        assertFirstDayOfEpicWeekInYear(2014, 2013, 12, 29);
        assertFirstDayOfEpicWeekInYear(2013, 2012, 12, 30);
        assertFirstDayOfEpicWeekInYear(2012, 2012, 1, 1);
        assertFirstDayOfEpicWeekInYear(2011, 2011, 1, 2);
        assertFirstDayOfEpicWeekInYear(2010, 2010, 1, 3);
        assertFirstDayOfEpicWeekInYear(2009, 2009, 1, 4);
        assertFirstDayOfEpicWeekInYear(2008, 2007, 12, 30);
        assertFirstDayOfEpicWeekInYear(2007, 2006, 12, 31);
    }

    @Test
    public void weekOfYear() {
        assertWeekOfYear(2016, 1, 3, 1, 2016);
        assertWeekOfYear(2015, 1, 1, 53, 2014);
        assertWeekOfYear(2015, 1, 4, 1, 2015);
        assertWeekOfYear(2015, 2, 2, 5, 2015);
        assertWeekOfYear(2013, 1, 1, 1, 2013);
        assertWeekOfYear(2013, 1, 30, 5, 2013);
        assertWeekOfYear(2013, 3, 4, 10, 2013);
        assertWeekOfYear(2013, 7, 31, 31, 2013);
        assertWeekOfYear(2013, 8, 14, 33, 2013);
        assertWeekOfYear(2013, 11, 14, 46, 2013);
        assertWeekOfYear(2013, 12, 11, 50, 2013);
        assertWeekOfYear(2013, 12, 28, 52, 2013);
        assertWeekOfYear(2013, 12, 29, 1, 2014);
        assertWeekOfYear(2013, 12, 30, 1, 2014);
        assertWeekOfYear(2013, 12, 31, 1, 2014);
        assertWeekOfYear(2012, 1, 1, 1, 2012);
        assertWeekOfYear(2012, 1, 2, 1, 2012);
        assertWeekOfYear(2011, 1, 1, 52, 2010);
        assertWeekOfYear(2011, 2, 2, 5, 2011);
        assertWeekOfYear(2011, 4, 27, 17, 2011);
        assertWeekOfYear(2011, 12, 2, 48, 2011);
        assertWeekOfYear(2011, 12, 29, 52, 2011);
        assertWeekOfYear(2011, 12, 31, 52, 2011);
    }

    @Test
    public void rangeByDate() {
        assertRangeByDate(2013, 1, new LocalDate(2012, 12, 30), new LocalDate(2013, 1, 5));
        assertRangeByDate(2013, 9, new LocalDate(2013, 2, 24), new LocalDate(2013, 3, 2));
        assertRangeByDate(2013, 27, new LocalDate(2013, 6, 30), new LocalDate(2013, 7, 6));
        assertRangeByDate(2013, 52, new LocalDate(2013, 12, 22), new LocalDate(2013, 12, 28));
    }

    private static void assertRangeByDate(int year, int weekInYear, LocalDate startDate, LocalDate endDate) {
        DateRange range = CalendarUtils.rangeByEpiWeek(new EpiWeek(weekInYear, year));
        assertEquals(range, new DateRange(startDate.atMidnightInMyTimezone(), endDate.atMidnightInMyTimezone()));
    }

    private static void assertWeekOfYear(int year, int month, int dayOfMonth, int expectedWeek, int expectedYear) {
        Date date = new LocalDate(year, month, dayOfMonth).atMidnightInMyTimezone();
        EpiWeek epiWeek = CalendarUtils.epiWeek(date);
        assertEquals(epiWeek.getWeekInYear(), expectedWeek);
        assertEquals(epiWeek.getYear(), expectedYear);
    }

    private Date firstDayOfEpicWeekInYear(int year) {
        return CalendarUtils.firstDayOfEpicWeekInYear(year);
    }

    private void assertFirstDayOfEpicWeekInYear(int year, int expectedYear, int expectedMonth, int expectedDayOfMonth) {
        LocalDate localDate = new LocalDate(expectedYear, expectedMonth, expectedDayOfMonth);
        assertEquals(firstDayOfEpicWeekInYear(year), localDate.atMidnightInMyTimezone());
    }

    public static DateShifter jvmDateShifter() {
        return new DateShifter() {
            @Override
            public void addMonthsToDate(Date date, int add) {
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                c.add(Calendar.MONTH, add);
                date.setTime(c.getTime().getTime());
            }

            @Override
            public void addDaysToDate(Date date, int add) {
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                c.add(Calendar.DAY_OF_MONTH, add);
                date.setTime(c.getTime().getTime());
            }
        };
    }

    @Test
    public void lastFourQuarters() {

        LocalDate date = new LocalDate(2015, 6, 7);
        ArrayList<LocalDateRange> ranges = Lists.newArrayList(CalendarUtils.getLastFourQuarterMap(date, jvmDateShifter()).values());

        assertEquals(ranges.get(0), new LocalDateRange(new LocalDate(2015, 4, 1), new LocalDate(2015, 6, 30)));
        assertEquals(ranges.get(1), new LocalDateRange(new LocalDate(2015, 1, 1), new LocalDate(2015, 3, 31)));
        assertEquals(ranges.get(2), new LocalDateRange(new LocalDate(2014, 10, 1), new LocalDate(2014, 12, 31)));
        assertEquals(ranges.get(3), new LocalDateRange(new LocalDate(2014, 7, 1), new LocalDate(2014, 9, 30)));
    }
}
