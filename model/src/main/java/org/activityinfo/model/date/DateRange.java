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
package org.activityinfo.model.date;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * @author yuriyz on 02/05/2015.
 */
public class DateRange implements Serializable {

    private Date start;
    private Date end;

    public DateRange() {
    }

    public DateRange(Date start, Date end) {
        this.start = start;
        this.end = end;
    }

    public DateRange(org.activityinfo.model.type.time.LocalDate start, org.activityinfo.model.type.time.LocalDate end) {
        this.start = start.atMidnightInMyTimezone();
        this.end = end.atMidnightInMyTimezone();
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public boolean isValid() {
        return start != null && end != null && start.getTime() <= end.getTime();
    }

    public boolean isValidWithNull() {
        return start == null || end == null || start.getTime() <= end.getTime();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DateRange that = (DateRange) o;
        return Objects.equals(this.start, that.start) && Objects.equals(this.end, that.end);
    }

    @Override
    public int hashCode() {
        int result = start != null ? start.hashCode() : 0;
        result = 31 * result + (end != null ? end.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DateRange{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}