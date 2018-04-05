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

import static org.activityinfo.model.type.time.LocalDateInterval.month;
import static org.activityinfo.model.type.time.LocalDateInterval.year;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LocalDateIntervalTest {

    @Test
    public void overlaps() {
        assertFalse(year(2017).overlaps(year(2018)));
        assertTrue(month(2017, 1).overlaps(year(2017)));
        assertFalse(month(2017, 1).overlaps(month(2017, 2)));
    }

}