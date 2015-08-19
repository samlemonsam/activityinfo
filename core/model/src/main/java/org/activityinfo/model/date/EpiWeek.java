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

import java.io.Serializable;

/**
 * Each epi week begins on a Sunday and ends on a Saturday.
 * The first epi week of the year ends, by definition, on the first Saturday of January,
 * as long as it falls at least four days into the month.
 *
 * @author yuriyz on 02/26/2015.
 */
public class EpiWeek implements Serializable {
    private int weekInYear;
    private int year;

    public EpiWeek() {
    }

    public EpiWeek(int weekInYear, int year) {
        this.weekInYear = weekInYear;
        this.year = year;
        Preconditions.checkState(weekInYear >= 1 && weekInYear <= 53, "Bug! Week number must be between [1..53] but is " + weekInYear);
        Preconditions.checkState(year > 0, "Year must be more than zero. Year: " + year);
    }

    public int getWeekInYear() {
        return weekInYear;
    }

    public EpiWeek setWeekInYear(int weekInYear) {
        this.weekInYear = weekInYear;
        Preconditions.checkState(weekInYear >= 1 && weekInYear <= 53, "Bug! Week number must be between [1..53] but is " + weekInYear);
        return this;
    }

    public int getYear() {
        return year;
    }

    public EpiWeek setYear(int year) {
        this.year = year;
        Preconditions.checkState(year > 0, "Year must be more than zero. Year: " + year);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EpiWeek epiWeek = (EpiWeek) o;
        if (weekInYear != epiWeek.weekInYear) return false;
        if (year != epiWeek.year) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = weekInYear;
        result = 31 * result + year;
        return result;
    }

    @Override
    public String toString() {
        return "EpiWeek{" +
                "weekInYear=" + weekInYear +
                ", year=" + year +
                '}';
    }
}