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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class LocalDateTest {

    @Test
    public void negYear() {
        try {
            LocalDate negYearDate = new LocalDate(-100,1,1);
            throw new AssertionError("Should not be able to enter negative years (i.e. BCE)");
        } catch (IllegalStateException excp) { /* Good - prevented from entering negative years */ }
    }

    @Test
    public void zeroYear() {
        try {
            LocalDate zeroYearDate = new LocalDate(0,1,1);
            throw new AssertionError("Should not be able to enter zero years (i.e. 1 BCE)");
        } catch (IllegalStateException excp) { /* Good - prevented from entering zero years */ }
    }

    @Test
    public void minDate() {
        LocalDate afterMinDate = new LocalDate(2017,01,01);
        assertTrue("Should be _after_ minimum date", afterMinDate.after(LocalDate.MIN_DATE));

        LocalDate onMinDate = new LocalDate(1000,01,01);
        assertTrue("Should be _on_ minimum date", onMinDate.equals(LocalDate.MIN_DATE));

        LocalDate beforeMinDate = new LocalDate(99,01,01);
        assertTrue("Should be _before_ minimum date", beforeMinDate.before(LocalDate.MIN_DATE));
    }

    @Test
    public void quarter() {
        assertThat(quarterOfMonth(1),  equalTo(1));
        assertThat(quarterOfMonth(2),  equalTo(1));
        assertThat(quarterOfMonth(3),  equalTo(1));
        
        assertThat(quarterOfMonth(4),  equalTo(2));
        assertThat(quarterOfMonth(5),  equalTo(2));
        assertThat(quarterOfMonth(6),  equalTo(2));
        
        assertThat(quarterOfMonth(7),  equalTo(3));
        assertThat(quarterOfMonth(8),  equalTo(3));
        assertThat(quarterOfMonth(9),  equalTo(3));
        
        assertThat(quarterOfMonth(10), equalTo(4));
        assertThat(quarterOfMonth(11), equalTo(4));
        assertThat(quarterOfMonth(12), equalTo(4));

    }

    @Test
    public void dayOfYear() {
        // non leap year
        assertThat(new LocalDate(2017, 1, 1).getDayOfYear(), equalTo(1));
        assertThat(new LocalDate(2017, 2, 1).getDayOfYear(), equalTo(32));
        assertThat(new LocalDate(2017, 2, 10).getDayOfYear(), equalTo(41));
        assertThat(new LocalDate(2017, 3, 4).getDayOfYear(), equalTo(63));
        assertThat(new LocalDate(2017, 7, 13).getDayOfYear(), equalTo(194));
        assertThat(new LocalDate(2017, 12, 31).getDayOfYear(), equalTo(365));

        // leap year
        assertThat(new LocalDate(2004, 1, 1).getDayOfYear(), equalTo(1));
        assertThat(new LocalDate(2004, 2, 1).getDayOfYear(), equalTo(32));
        assertThat(new LocalDate(2004, 2, 10).getDayOfYear(), equalTo(41));
        assertThat(new LocalDate(2004, 3, 4).getDayOfYear(), equalTo(64));
        assertThat(new LocalDate(2004, 7, 13).getDayOfYear(), equalTo(195));
        assertThat(new LocalDate(2004, 12, 31).getDayOfYear(), equalTo(366));
    }


    private int quarterOfMonth(int month) {
        LocalDate date = new LocalDate(2015, month, 1);
        return date.getQuarter();
    }
}