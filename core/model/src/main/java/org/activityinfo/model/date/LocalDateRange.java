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

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;
import java.util.Date;

/**
 * @author yuriyz on 07/03/2015.
 */
public class LocalDateRange implements Serializable {

    private LocalDate minDate;

    private LocalDate maxDate;

    /**
     * Constructs a <code>DateRange</code> bounded by <code>minDate</code> and
     * <code>maxDate</code>
     *
     * @param minDate The minimum date to be included in this range (inclusive), or
     *                <code>null</code> if there is no minimum bound
     * @param maxDate The maximum date to be included in this range (inclusive), or
     *                <code>null</code> if there is no maximum bound.
     */
    public LocalDateRange(Date minDate, Date maxDate) {
        this.setMinDate(minDate);
        this.setMaxDate(maxDate);
    }

    public LocalDateRange(LocalDate minDate, LocalDate maxDate) {
        this.setMinDate(minDate);
        this.setMaxDate(maxDate);
    }

    /**
     * Constructs a fully open date range (all dates are included).
     */
    public LocalDateRange() {
        setMinDate((LocalDate) null);
        setMaxDate((LocalDate) null);
    }

    /**
     * Gets the minimum date in this range (inclusive).
     *
     * @return The minimum date in this range (inclusive) or <code>null</code>
     * if the range has no lower bound
     */
    @XmlAttribute(name = "min")
    public Date getMinDate() {
        return minDate == null ? null : minDate.atMidnightInMyTimezone();
    }

    public LocalDate getMinLocalDate() {
        return minDate;
    }

    /**
     * Sets the minimum date in this range (inclusive).
     *
     * @param minDate The minimum date in this range (inclusive) or
     *                <code>null</code> if the range has now upper bound.
     */
    public void setMinDate(Date minDate) {
        this.minDate = minDate == null ? null : new LocalDate(minDate);
    }

    public void setMinDate(LocalDate minDate) {
        this.minDate = minDate;
    }

    /**
     * Gets the maximum date in this range (inclusive).
     *
     * @return The maximum date in this range (inclusive) or <code>null</code>
     * if the range has no upper bound.
     */
    @XmlAttribute(name = "max")
    public Date getMaxDate() {
        return maxDate == null ? null : maxDate.atMidnightInMyTimezone();
    }

    public LocalDate getMaxLocalDate() {
        return maxDate;
    }

    /**
     * Sets the maximum date in this range (inclusive).
     *
     * @param maxDate The maximum date in this range (inclusive) or
     *                <code>null</code> if the range has no upper bound.
     */
    public void setMaxDate(Date maxDate) {
        this.maxDate = maxDate == null ? null : new LocalDate(maxDate);
    }

    public void setMaxDate(LocalDate maxDate) {
        this.maxDate = maxDate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((maxDate == null) ? 0 : maxDate.hashCode());
        result = prime * result + ((minDate == null) ? 0 : minDate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LocalDateRange other = (LocalDateRange) obj;
        if (maxDate == null) {
            if (other.maxDate != null) {
                return false;
            }
        } else if (!maxDate.equals(other.maxDate)) {
            return false;
        }
        if (minDate == null) {
            if (other.minDate != null) {
                return false;
            }
        } else if (!minDate.equals(other.minDate)) {
            return false;
        }
        return true;
    }

}