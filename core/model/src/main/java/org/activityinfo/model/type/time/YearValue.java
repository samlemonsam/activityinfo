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

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.activityinfo.model.resource.IsRecord;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;

/**
 * Represents a specific calendar year in the ISO-8601 calendar.
 */
public class YearValue implements FieldValue, IsRecord, TemporalValue {

    private final int year;

    public YearValue(int year) {
        this.year = year;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        YearValue yearValue = (YearValue) o;

        if (year != yearValue.year) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return year;
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return YearType.TYPE_CLASS;
    }

    @Override
    public JsonElement toJsonElement() {
        return new JsonPrimitive(year);
    }

    @Override
    public Record asRecord() {
        return new Record().set("year", year);
    }

    @Override
    public LocalDateInterval asInterval() {
        return new LocalDateInterval(new LocalDate(year, 1, 1), new LocalDate(year, 12, 31));
    }

    public int getYear() {
        return year;
    }
}
