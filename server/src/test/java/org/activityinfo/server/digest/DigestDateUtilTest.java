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
package org.activityinfo.server.digest;

import org.activityinfo.server.util.date.DateCalc;
import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class DigestDateUtilTest {

    private Date createDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2013, 3, 18, 15, 6, 30);
        return cal.getTime();
    }

    @Test
    public void testIsOnToday() {
        // morning
        Date d = createDate();

        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 10);
        Date c = cal.getTime();
        Assert.assertTrue(DateCalc.isOnToday(d, c));

        cal.add(Calendar.MINUTE, -20);
        c = cal.getTime();
        Assert.assertFalse(DateCalc.isOnToday(d, c));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsOnTodayTomorrow() {
        // evening
        Date d = createDate();

        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        Date c = cal.getTime();
        Assert.assertTrue(DateCalc.isOnToday(d, c));

        cal.add(Calendar.MINUTE, 5);
        c = cal.getTime();
        // should throw
        Assert.assertFalse(DateCalc.isOnToday(d, c));
    }

    @Test
    public void testIsOnYesterday() {
        Date d = createDate();

        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 10);
        Date c = cal.getTime();
        Assert.assertFalse(DateCalc.isOnYesterday(d, c));

        cal.add(Calendar.MINUTE, -20);
        c = cal.getTime();
        Assert.assertTrue(DateCalc.isOnYesterday(d, c));
    }

    @Test
    public void testDaysBeforeMidnight() {
        Date d = createDate();

        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 10);
        Date c = cal.getTime();
        Assert.assertEquals(0, DateCalc.daysBeforeMidnight(d, c));

        cal.add(Calendar.MINUTE, -20);
        c = cal.getTime();
        Assert.assertEquals(1, DateCalc.daysBeforeMidnight(d, c));

        cal.setTime(d);
        cal.add(Calendar.DATE, -1);
        cal.add(Calendar.MINUTE, 5);
        Assert.assertEquals(1, DateCalc.daysBeforeMidnight(d, c));

        cal.add(Calendar.MINUTE, -10);
        Assert.assertEquals(1, DateCalc.daysBeforeMidnight(d, c));
    }

    @Test
    public void testAbsoluteDaysBetween() {
        Date d = createDate();

        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 10);
        Date c = cal.getTime();
        Assert.assertEquals(0, DateCalc.absoluteDaysBetween(d, c));

        cal.add(Calendar.MINUTE, -20);
        c = cal.getTime();
        Assert.assertEquals(0, DateCalc.absoluteDaysBetween(d, c));

        cal.setTime(d);
        cal.add(Calendar.DATE, -1);
        cal.add(Calendar.MINUTE, 5);
        c = cal.getTime();
        Assert.assertEquals(0, DateCalc.absoluteDaysBetween(d, c));

        cal.add(Calendar.MINUTE, -10);
        c = cal.getTime();
        Assert.assertEquals(1, DateCalc.absoluteDaysBetween(d, c));
    }
}
