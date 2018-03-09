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

public class FortnightValue implements PeriodValue {

    private final EpiWeek startWeek;

    public FortnightValue(EpiWeek startWeek) {
        this.startWeek = startWeek;
    }

    public FortnightValue(int year, int weekNum) {
        this(new EpiWeek(year, weekNum));
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return FortnightType.TYPE_CLASS;
    }

    @Override
    public JsonValue toJson() {
        return startWeek.toJson();
    }

    public int getYear() {
        return startWeek.getYear();
    }

    @Override
    public LocalDateInterval asInterval() {
        LocalDate startDate = startWeek.asInterval().getStartDate();
        LocalDate endDate = startDate.plusDays(13);

        return new LocalDateInterval(startDate, endDate);
    }

    @Override
    public PeriodValue previous() {
        return new FortnightValue(startWeek.plus(-2));
    }

    @Override
    public PeriodValue next() {
        return new FortnightValue(startWeek.plus(+2));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FortnightValue that = (FortnightValue) o;

        return startWeek.equals(that.startWeek);

    }

    @Override
    public int hashCode() {
        return startWeek.hashCode();
    }

    @Override
    public String toString() {
        return getYear() + toWeekString();
    }

    public String toWeekString() {
        int startWeekNumber = startWeek.getWeekInYear();
        return "W" + startWeekNumber + "-W" + (startWeekNumber+1);
    }

    public int getWeekInYear() {
        return startWeek.getWeekInYear();
    }
}
