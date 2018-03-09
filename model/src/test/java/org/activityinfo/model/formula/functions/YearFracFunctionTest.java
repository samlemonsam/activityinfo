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
package org.activityinfo.model.formula.functions;

import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.time.LocalDate;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests the YEARFRAC() calculation
 */
public class YearFracFunctionTest {

    @Test
    public void wholeYears() {
        check("2000-01-01", "2016-01-01", 16.0);
        check("2016-01-01", "2000-01-01", 16.0);
    }

    @Test
    public void odsSuite() {
        // Source: https://lists.oasis-open.org/archives/oic/201004/msg00023.html
        check("2001-01-01", "2002-01-01", 1.000000);
        check("2002-01-01", "2001-01-01", 1.000000);
        check("2000-01-01", "2000-01-01", 0.000000);
        check("2002-01-01", "2002-01-01", 0.000000);
        check("2010-01-31", "2010-07-31", 0.500000);
        check("2010-01-31", "2010-08-01", 0.502778);
        check("2010-01-30", "2010-07-31", 0.500000);
        check("2010-02-28",	"2011-02-28", 1.000000);
        check("2000-02-29",	"2008-02-29", 8.000000);
        check("2010-02-28",	"2010-08-30", 0.500000);
        check("2008-02-29",	"2008-08-30", 0.500000);
    }

    @Test
    public void evaluation() {
        LocalDate x = new LocalDate(2016, 1, 1);
        LocalDate y = new LocalDate(2017, 1, 1);
        Quantity z = (Quantity) YearFracFunction.INSTANCE.apply(Arrays.<FieldValue>asList(x, y));

        assertThat(z, equalTo(new Quantity(1.0)));
    }

    private void check(String startDateString, String endDateString, double expectedFrac) {
        LocalDate startDate = LocalDate.parse(startDateString);
        LocalDate endDate = LocalDate.parse(endDateString);

        double frac = YearFracFunction.compute(startDate, endDate);

        double diff = Math.abs(frac - expectedFrac);
        if(diff > 0.001) {
            throw new AssertionError(String.format("Expected YEARFRAC(%s, %s) = %10.5f, but was %10.5f",
                    startDateString,
                    endDateString,
                    expectedFrac,
                    frac));
        }
    }
}