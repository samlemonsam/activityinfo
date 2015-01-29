package org.activityinfo.model.type.period;
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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author yuriyz on 01/27/2015.
 */
public class Iso8601Test {

    @Test
    public void parseTest() {
        assertPeriod("P1Y", new PeriodValue().setYear(1));
        assertPeriod("PT36H", new PeriodValue().setHour(36));
        assertPeriod("P1DT12H", new PeriodValue().setDay(1).setHour(12));
    }

    @Test(expected = RuntimeException.class)
    public void negative() {
        assertPeriod("", new PeriodValue().setYear(1));
        assertPeriod("P", new PeriodValue().setYear(1));
        assertPeriod("1Y", new PeriodValue().setYear(1));
        assertPeriod("PY", new PeriodValue().setYear(1));
        assertPeriod("PYT", new PeriodValue().setYear(1));
        assertPeriod("PYT", new PeriodValue().setYear(1));
        assertPeriod("PYT1", new PeriodValue().setYear(1));
        assertPeriod("P1YT1", new PeriodValue().setYear(1));
        assertPeriod("P1H", new PeriodValue().setYear(1));
        assertPeriod("a", new PeriodValue().setYear(1));
    }

    private void assertPeriod(String periodAsIso8601, PeriodValue periodValue) {
        PeriodValue parsed = Iso8601.parse(periodAsIso8601);
        assertEquals(parsed, periodValue);
        assertEquals(periodAsIso8601, Iso8601.asIso8601String(periodValue));
    }
}
