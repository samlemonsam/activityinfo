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
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;

/**
 * @author yuriyz on 07/06/2015.
 */
public class CalendarUtilsTest {

    @Test
    public void lastFourQuarters() {

        LocalDate date = new LocalDate(2015, 6, 7);
        ArrayList<LocalDateRange> ranges = Lists.newArrayList(CalendarUtils.getLastFourQuarterMap(date).values());

        assertEquals(ranges.get(0), new LocalDateRange(new LocalDate(2015, 4, 1), new LocalDate(2015, 6, 30)));
        assertEquals(ranges.get(1), new LocalDateRange(new LocalDate(2015, 1, 1), new LocalDate(2015, 3, 31)));
        assertEquals(ranges.get(2), new LocalDateRange(new LocalDate(2014, 10, 1), new LocalDate(2014, 12, 31)));
        assertEquals(ranges.get(3), new LocalDateRange(new LocalDate(2014, 7, 1), new LocalDate(2014, 9, 30)));
    }
}
