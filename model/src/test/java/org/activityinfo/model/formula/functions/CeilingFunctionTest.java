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
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertThat;

public class CeilingFunctionTest {

    @Test
    public void test() {
        assertThat(CeilingFunction.INSTANCE.apply(1.6), Matchers.equalTo(2.0));
        assertThat(CeilingFunction.INSTANCE.apply(1.5), Matchers.equalTo(2.0));
        assertThat(CeilingFunction.INSTANCE.apply(1.4), Matchers.equalTo(2.0));

        assertThat(CeilingFunction.INSTANCE.apply(-1.6), Matchers.equalTo(-1.0));
        assertThat(CeilingFunction.INSTANCE.apply(-1.5), Matchers.equalTo(-1.0));
        assertThat(CeilingFunction.INSTANCE.apply(-1.4), Matchers.equalTo(-1.0));

        assertThat(CeilingFunction.INSTANCE.apply(  Collections.<FieldValue>singletonList(new Quantity(1.5))),
                                                    Matchers.<FieldValue>equalTo(new Quantity(2.0)));
        assertThat(CeilingFunction.INSTANCE.apply(  Collections.<FieldValue>singletonList(new Quantity(-1.5))),
                                                    Matchers.<FieldValue>equalTo(new Quantity(-1.0)));
    }

}