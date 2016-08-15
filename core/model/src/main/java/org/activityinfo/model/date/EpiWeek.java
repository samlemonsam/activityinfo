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
import com.google.common.base.Preconditions;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.datepicker.client.CalendarUtil;

import java.io.Serializable;
import java.util.Date;

/**
 * Each epi week begins on a Sunday and ends on a Saturday.
 * The first epi week of the year ends, by definition, on the first Saturday of January,
 * as long as it falls at least four days into the month.
 *
 * @author yuriyz on 02/26/2015.
 */
public class EpiWeek implements Serializable {

    public static final int WEEKS_IN_YEAR = 52;

    private int weekInYear;
    private int year;

    /**
     * Uninitialized epi week.
     */
    public EpiWeek() {
    }

    public EpiWeek(int weekInYear, int year) {
        this.weekInYear = weekInYear;
        this.year = year;

        normalize();
        checkState();
    }

    private void normalize() {
        while (weekInYear > WEEKS_IN_YEAR) {
            year++;
            weekInYear -= WEEKS_IN_YEAR;
        }
        while (weekInYear < 1) {
            year--;
            weekInYear += WEEKS_IN_YEAR;
        }
    }

    private void checkState() {
        checkState(true, true);
    }

    private void checkState(boolean checkYear, boolean checkWeek) {
        if (checkWeek) {
            Preconditions.checkState(weekInYear >= 1 && weekInYear <= 53, "Bug! Week number must be between [1..53] but is " + weekInYear);
        }
        if (checkYear) {
            Preconditions.checkState(year > 0, "Year must be more than zero. Year: " + year);
        }
    }

    public int getWeekInYear() {
        return weekInYear;
    }

    public EpiWeek setWeekInYear(int weekInYear) {
        this.weekInYear = weekInYear;
        checkState(false, true);
        return this;
    }

    public int getYear() {
        return year;
    }

    public EpiWeek setYear(int year) {
        this.year = year;
        checkState(true, false);
        return this;
    }

    public EpiWeek plus(int count) {
        return new EpiWeek(weekInYear + count, year);
    }

    public EpiWeek next() {
        return plus(+1);
    }

    public EpiWeek previous() {
        return plus(-1);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EpiWeek epiWeek = (EpiWeek) o;
        return weekInYear == epiWeek.weekInYear && year == epiWeek.year;
    }

    @Override
    public int hashCode() {
        int result = weekInYear;
        result = 31 * result + year;
        return result;
    }

    @Override
    public String toString() {
        return year + "W" + weekInYear;
    }

    /**
     * @param epiWeekAsString (e.g. 2015W1, 2016W30)
     * @return epi week
     */
    public static EpiWeek parse(String epiWeekAsString) {
        String[] tokens = epiWeekAsString.split("W");
        if (tokens.length != 2) {
            throw new NumberFormatException();
        }
        String year = tokens[0];
        String weekInYear = tokens[1];
        return new EpiWeek(Integer.parseInt(weekInYear), Integer.parseInt(year));
    }

    public DateRange getDateRange() {
        Date date = new LocalDate(getYear(), 1, 1).atMidnightInMyTimezone();
        int dayInYearInsideWeek = getWeekInYear() * 7 - 4;

        if (GWT.isClient()) {
            CalendarUtil.addDaysToDate(date, dayInYearInsideWeek);
            DayOfWeek dateOfWeek = DayOfWeek.dayOfWeek(date);

            Date startDate = CalendarUtil.copyDate(date);
            CalendarUtil.addDaysToDate(startDate, -dateOfWeek.getValue());
            Date endDate = CalendarUtil.copyDate(date);
            CalendarUtil.addDaysToDate(endDate, 6 - dateOfWeek.getValue());
            return new DateRange(startDate, endDate);
        } else {
            throw new UnsupportedOperationException("todo");
        }
    }
}