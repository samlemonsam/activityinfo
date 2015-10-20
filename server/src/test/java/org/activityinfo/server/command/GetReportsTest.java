package org.activityinfo.server.command;

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

import com.google.common.base.Stopwatch;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.command.GetReports;
import org.activityinfo.legacy.shared.command.result.ReportsResult;
import org.activityinfo.server.database.OnDataSet;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/getreporttests.db.xml")
public class GetReportsTest extends CommandTestCase {

    @Test
    public void selectByUser1() {
        setUser(1);
        ReportsResult result = execute(new GetReports());
        assertNotNull(result);
        assertTrue(result.getData().size() == 2);
        assertEquals(1, result.getData().get(0).getId());
        assertEquals("Report 1", result.getData().get(0).getTitle());
        assertEquals("Alex", result.getData().get(0).getOwnerName());
        assertEquals(3, result.getData().get(1).getId());
        assertEquals("Report 3", result.getData().get(1).getTitle());
        assertEquals("Alex", result.getData().get(1).getOwnerName());
    }

    @Test
    public void selectByUser2() {
        setUser(2);
        ReportsResult result = execute(new GetReports());
        assertNotNull(result);
        assertThat(result.getData().size(), equalTo(1));
        assertEquals(2, result.getData().get(0).getId());
        assertEquals("Report 1", result.getData().get(0).getTitle());
        assertEquals("Bavon", result.getData().get(0).getOwnerName());
    }

    @OnDataSet("/dbunit/get-report-performance-tests.db.xml")
    @Test // AI-1223
    public void performanceTest() {
        // initial performance: 1838ms - sql fetch, 1962ms - time for client (+serialization/deserialization)
        // after performance tuning: 229ms - sql fetch, 273 ms - time for client (+serialization/deserialization)

        setUser(1);

        Stopwatch started = Stopwatch.createStarted();

        ReportsResult result = execute(new GetReports());
        assertNotNull(result);
        assertEquals(1, result.getData().get(0).getId());
        assertEquals("Report 1", result.getData().get(0).getTitle());
        assertEquals("Alex", result.getData().get(0).getOwnerName());

        long elapsed = started.elapsed(TimeUnit.MILLISECONDS);
        assertTrue("GetReports takes " + elapsed + "ms.", elapsed < 1000); // must be less then one second
    }

}
