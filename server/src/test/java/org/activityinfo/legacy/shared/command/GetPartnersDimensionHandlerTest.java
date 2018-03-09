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
package org.activityinfo.legacy.shared.command;

import com.bedatadriven.rebar.time.calendar.LocalDate;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.command.result.PartnerResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(InjectionSupport.class)
public class GetPartnersDimensionHandlerTest extends CommandTestCase2 {

    // empty
    @Test
    @OnDataSet("/dbunit/sites-simple1.db.xml")
    public void testEmptyFilter() throws CommandException {
        PartnerResult result = this.execute();
        assertThat(result.getData().size(), equalTo(0));
    }


    // data entry filter population query
    @Test
    @OnDataSet("/dbunit/sites-simple1.db.xml")
    public void testActivity() throws CommandException {
        PartnerResult result = execute(DimensionType.Activity, 1);
        assertThat(result.getData().size(), equalTo(2));
        assertThat(result.getData().get(0).getName(), equalTo("NRC"));
        assertThat(result.getData().get(1).getName(), equalTo("Solidarites"));
    }

    @Test
    @OnDataSet("/dbunit/sites-simple1.db.xml")
    public void testActivityWithDateFilter() throws CommandException {

        Filter filter = new Filter();
        filter.addRestriction(DimensionType.Activity, 1);
        filter.getEndDateRange().setMinDate(new LocalDate(1998,1,1).atMidnightInMyTimezone());
        filter.getEndDateRange().setMaxDate(new LocalDate(2099,1,15).atMidnightInMyTimezone());

        PartnerResult result = execute(filter);
        assertThat(result.getData().size(), equalTo(2));
        assertThat(result.getData().get(0).getName(), equalTo("NRC"));
        assertThat(result.getData().get(1).getName(), equalTo("Solidarites"));
    }

    @Test
    @OnDataSet("/dbunit/sites-simple1.db.xml")
    public void testSiteCountIndicators() throws CommandException {
        /*
        Activity #1
        Indicator #103 (site count)
        Indicator #675 (site count)
        
        Site #1: Partner #1
        Site #2: Partner #1
        Site #3: partner #2
        */
        
        // empty
        PartnerResult result = execute(DimensionType.Indicator, 103, 675);
        
        // indicators 103 and 675 are site count indicators, so all sites of activity #1 
        // should have a non empty value and be considered present
        
        assertThat(result.getData().size(), equalTo(2));
    }

    @Test
    @OnDataSet("/dbunit/sites-simple1.db.xml")
    public void testIndicatorValues() throws CommandException {
        // NRC, Solidarites
        PartnerResult result = execute(DimensionType.Indicator, 1, 2);
        assertThat(result.getData().size(), equalTo(2));
        assertThat(result.getData().get(0).getName(), equalTo("NRC"));
        assertThat(result.getData().get(1).getName(), equalTo("Solidarites"));
    }

    @Test
    @OnDataSet("/dbunit/sites-linked.db.xml")
    public void testIndicatorLinked100() throws CommandException {
        // empty
        PartnerResult result = execute(DimensionType.Indicator, 100);
        assertThat(result.getData().size(), equalTo(0));
    }

    @Test
    @OnDataSet("/dbunit/sites-linked.db.xml")
    public void testIndicatorLinked1() throws CommandException {
        // NRC, NRC2
        PartnerResult result = execute(DimensionType.Indicator, 1);
        assertThat(result.getData().size(), equalTo(2));
        assertThat(result.getData().get(0).getName(), equalTo("NRC"));
        assertThat(result.getData().get(1).getName(), equalTo("NRC2"));
    }

    @Test
    @OnDataSet("/dbunit/sites-linked.db.xml")
    public void testIndicatorLinked2() throws CommandException {
        /*
        Database #1 > Activity #1 (once)
        Site #2: Partner #1 
        I1=400
        
        Database #2 > Activity #2 (once)
        Site #1: Partner #1
        I3=1500
        
        Links
        I3 -> I2
        */
        // NRC
        PartnerResult result = execute(DimensionType.Indicator, 2);
        assertThat(result.getData().size(), equalTo(1));
        assertThat(result.getData().get(0).getName(), equalTo("NRC"));
    }

    @Test
    @OnDataSet("/dbunit/sites-linked.db.xml")
    public void testIndicatorLinked12100() throws CommandException {
        // NRC, NRC2
        PartnerResult result = execute(DimensionType.Indicator, 1, 2, 100);
        assertThat(result.getData().size(), equalTo(2));
        assertThat(result.getData().get(0).getName(), equalTo("NRC"));
        assertThat(result.getData().get(1).getName(), equalTo("NRC2"));
    }


    private PartnerResult execute() {
        return this.execute(null, (Integer[]) null);
    }

    private PartnerResult execute(DimensionType type, Integer... params) {
        setUser(1);
        Filter filter = new Filter();
        if (type != null) {
            filter.addRestriction(type, Arrays.asList(params));
        }
        PartnerResult results = execute(filter);
        System.out.println(results.getData());

        return results;
    }

    private PartnerResult execute(Filter filter) {
        return execute(new GetPartnersDimension(filter));
    }
}
