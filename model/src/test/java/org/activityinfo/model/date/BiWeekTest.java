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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by yuriyz on 8/15/2016.
 */
public class BiWeekTest {

    @Test
    public void serialization() {
        BiWeek week = new BiWeek(2, 2016);
        BiWeek deserialized = BiWeek.parse(week.toString());

        assertEquals(week, deserialized);
    }

    @Test
    public void plus() {
        BiWeek week = new BiWeek(2, 2016);

        assertEquals(week.plus(1), new BiWeek(4, 2016));
        assertEquals(week.plus(10), new BiWeek(22, 2016));
    }
}
