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
package org.activityinfo.model.type.time;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.FieldTypeClass;

import java.io.Serializable;

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
public class EpiWeek implements Serializable, PeriodValue {

    public static final int WEEKS_IN_YEAR = 52;

    private int weekInYear;
    private int year;

    public EpiWeek(EpiWeek week) {
        this(week.getYear(), week.getWeekInYear());
    }

    public EpiWeek(int year, int weekInYear) {
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
            assert weekInYear >= 1 && weekInYear <= 53 : "Week number must be between [1..53] but is " + weekInYear;
        }
        if (checkYear) {
            assert year > 0 : "Year must be more than zero. Year: " + year;
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
        return new EpiWeek(year, weekInYear + count);
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
        return toString(year, weekInYear);
    }

    public static String toString(int year, int weekInYear) {
        return year + "W" + weekInYear;
    }

    /**
     * @param epiWeekAsString (e.g. 2015W1, 2016W30)
     * @return epi week
     */
    public static EpiWeek parse(String epiWeekAsString) {
        //noinspection NonJREEmulationClassesInClientCode
        String[] tokens = epiWeekAsString.split("W");
        if (tokens.length != 2) {
            throw new NumberFormatException(epiWeekAsString);
        }
        String year = tokens[0];
        String weekInYear = tokens[1];
        return new EpiWeek(Integer.parseInt(year), Integer.parseInt(weekInYear));
    }



    /**
     * Calculates the date of the first epi-week of the year.
     */
    public static LocalDate dayOfFirstEpiWeek(int year) {
        // The first epi week of the year ends, by definition, on the first Saturday of January, as long as it falls
        // at least four days into the month.
        // Each epi week begins on a Sunday and ends on a Saturday.
        // http://www.cmmcp.org/epiweek.htm

        LocalDate jan1 = new LocalDate(year, 1, 1);
        switch (jan1.getDayOfWeek()) {
            case MONDAY:
                return new LocalDate(year - 1, 12, 31);
            case TUESDAY:
                return new LocalDate(year - 1, 12, 30);
            case WEDNESDAY:
                return new LocalDate(year - 1, 12, 29);
            case THURSDAY:
                return new LocalDate(year, 1, 4);
            case FRIDAY:
                return new LocalDate(year, 1, 3);
            case SATURDAY:
                return new LocalDate(year, 1, 2);
            case SUNDAY:
            default:
                return new LocalDate(year, 1, 1);
        }
    }

    public static EpiWeek weekOf(LocalDate date) {
        LocalDate firstDayOfYear = dayOfFirstEpiWeek(date.getYear());

        if(date.before(firstDayOfYear)) {
            return new EpiWeek(date.getYear() - 1, 52);
        } else {
            int daysBetween = LocalDate.daysBetween(firstDayOfYear, date);
            int weekNumber = daysBetween / 7;

            return new EpiWeek(date.getYear(), weekNumber + 1);
        }
    }

    @Override
    public LocalDateInterval asInterval() {
        LocalDate firstDayOfYear = dayOfFirstEpiWeek(this.year);
        LocalDate firstDayOfWeek = firstDayOfYear.plusDays((this.weekInYear - 1) * 7);
        LocalDate lastDayOfWeek = firstDayOfWeek.plusDays(6);
        return new LocalDateInterval(firstDayOfWeek, lastDayOfWeek);
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return EpiWeekType.TYPE_CLASS;
    }

    @Override
    public JsonValue toJson() {
        return Json.create(toString());
    }


}