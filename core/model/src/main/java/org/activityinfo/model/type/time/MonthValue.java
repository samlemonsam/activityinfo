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

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;

/**
 * Represents a specific calendar month in the ISO-8601 calendar.
 */
public class MonthValue implements FieldValue, TemporalValue {


    private final int year;
    private final int monthOfYear;

    /**
     *
     * @param year  complete calendar year, including century. For example: 1999, 2014
     * @param monthOfYear the month of the year, where January = 1 and December = 12
     */
    public MonthValue(int year, int monthOfYear) {
        assert monthOfYear >= 1 && monthOfYear <= 12;
        this.year = year;
        this.monthOfYear = monthOfYear;
    }

    public int getYear() {
        return year;
    }

    public int getMonthOfYear() {
        return monthOfYear;
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return MonthType.TYPE_CLASS;
    }

    @Override
    public JsonValue toJsonElement() {
        return Json.create(toString());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MonthValue that = (MonthValue) o;

        if (monthOfYear != that.monthOfYear) return false;
        if (year != that.year) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = year;
        result = 31 * result + monthOfYear;
        return result;
    }

    @Override
    public LocalDateInterval asInterval() {
        return new LocalDateInterval(new LocalDate(year, monthOfYear, 1), TimeUtils.getLastDayOfMonth(this));
    }

    @Override
    public String toString() {
        return year + (monthOfYear < 10 ? "-0" : "-") + monthOfYear;
    }
}