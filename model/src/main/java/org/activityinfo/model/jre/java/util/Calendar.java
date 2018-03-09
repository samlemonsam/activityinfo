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
package java.util;

import java.util.Date;

/**
 * Dummy calendar for super-source
 */
public class Calendar {

    public final static int DAY_OF_MONTH = 5;
    public final static int DAY_OF_YEAR = 6;
    public final static int DAY_OF_WEEK = 7;

    public Calendar() {
    }

    public static Calendar getInstance() {
        throw new UnsupportedOperationException();
    }

    public void setTime(Date date) {
        throw new UnsupportedOperationException();
    }

    public void add(int field, int amount) {
        throw new UnsupportedOperationException();
    }

    public final void set(int year, int month, int date) {
        throw new UnsupportedOperationException();
    }

    public final Date getTime() {
        throw new UnsupportedOperationException();
    }

    public int get(int field) {
        throw new UnsupportedOperationException();
    }

    public int getActualMaximum(int field) {
        throw new UnsupportedOperationException();
    }
}