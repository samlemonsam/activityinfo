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

import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;

import javax.annotation.Nonnull;

import static org.activityinfo.json.Json.createObject;

/**
 * {@code FieldValue} of type {@link org.activityinfo.model.type.time.LocalDateIntervalType} describing
 * a continuous interval between two {@link org.activityinfo.model.type.time.LocalDate}s,
 * starting on {@code startDate}, inclusive, and ending on {@code endDate}, inclusive.
 */
public class LocalDateInterval implements FieldValue {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public LocalDateInterval(@Nonnull LocalDate startDate, @Nonnull LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     *
     * @return the start date, inclusive of this interval
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * @return the end date, inclusive, of this interval
     */
    public LocalDate getEndDate() {
        return endDate;
    }


    /**
     * @return true if the {@code other} interval overlaps this interval with at least one day.
     */
    public boolean overlaps(LocalDateInterval other) {
        // Either the other interval finishes before this interval starts...
        // .. or it starts after this interval finishes

        return !(other.endDate.before(this.startDate) ||
                 other.startDate.after(this.endDate));
    }


    @Override
    public FieldTypeClass getTypeClass() {
        return LocalDateIntervalType.TYPE_CLASS;
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = createObject();
        object.put("start", startDate.toString());
        object.put("end", endDate.toString());
        return object;
    }

    public static LocalDateInterval fromJson(JsonValue object) {
        return new LocalDateInterval(
                LocalDate.parse(object.getString("start")),
                LocalDate.parse(object.getString("end")));
    }


    @Override
    public String toString() {
        return "[" + startDate + ", " + endDate + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocalDateInterval that = (LocalDateInterval) o;
        return this.startDate.equals(that.startDate) &&
               this.endDate.equals(that.endDate);
    }

    @Override
    public int hashCode() {
        int result = startDate != null ? startDate.hashCode() : 0;
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        return result;
    }

    public static LocalDateInterval year(int year) {
        return new LocalDateInterval(
                new LocalDate(year, 1, 1),
                new LocalDate(year, 12, 31));
    }

    public static LocalDateInterval month(int year, int month) {
        LocalDate start = new LocalDate(year, month, 1);
        LocalDate end = LocalDate.getLastDateOfMonth(year, month);

        return new LocalDateInterval(start, end);
    }
}
