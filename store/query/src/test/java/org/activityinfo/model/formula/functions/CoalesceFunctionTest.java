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

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.DoubleArrayColumnView;
import org.junit.Test;

import static java.lang.Double.NaN;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class CoalesceFunctionTest {

    @Test
    public void combineDouble() {
        DoubleArrayColumnView x = new DoubleArrayColumnView(new double[] { 80, NaN, 82, NaN,  84});
        DoubleArrayColumnView y = new DoubleArrayColumnView(new double[] { 90, NaN, 92,  93, NaN });

        ColumnView z = CoalesceFunction.combineDouble(new ColumnView[]{x, y});
        
        assertThat(z.getDouble(0), equalTo(80.0));
        assertThat(z.getDouble(1), equalTo(NaN));
        assertThat(z.getDouble(2), equalTo(82.0));
        assertThat(z.getDouble(3), equalTo(93.0));
        assertThat(z.getDouble(4), equalTo(84.0));
    }
}