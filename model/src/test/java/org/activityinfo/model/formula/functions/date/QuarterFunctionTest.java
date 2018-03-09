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
package org.activityinfo.model.formula.functions.date;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class QuarterFunctionTest {

    @Test
    public void test() {
        assertThat(QuarterFunction.fromMonth(1), equalTo(1));
        assertThat(QuarterFunction.fromMonth(2), equalTo(1));
        assertThat(QuarterFunction.fromMonth(3), equalTo(1));
        assertThat(QuarterFunction.fromMonth(4), equalTo(2));
        assertThat(QuarterFunction.fromMonth(5), equalTo(2));
        assertThat(QuarterFunction.fromMonth(6), equalTo(2));
        assertThat(QuarterFunction.fromMonth(7), equalTo(3));
        assertThat(QuarterFunction.fromMonth(8), equalTo(3));
        assertThat(QuarterFunction.fromMonth(9), equalTo(3));
        assertThat(QuarterFunction.fromMonth(10), equalTo(4));
        assertThat(QuarterFunction.fromMonth(11), equalTo(4));
        assertThat(QuarterFunction.fromMonth(12), equalTo(4));
    }
}