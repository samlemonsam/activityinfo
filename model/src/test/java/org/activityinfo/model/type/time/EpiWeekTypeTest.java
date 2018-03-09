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

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class EpiWeekTypeTest {

    @Test
    public void testParse() {
        assertThat(parseSubFormKey("s34234234-2017-2017W3"), equalTo(new EpiWeek(2017, 3)));
        assertThat(parseSubFormKey("s34234234-2017-2017W34"), equalTo(new EpiWeek(2017, 34)));
        assertThat(parseSubFormKey("s0417565614-2018W1"), equalTo(new EpiWeek(2018, 1)));
        assertThat(parseSubFormKey("s0417565614-2017W49"), equalTo(new EpiWeek(2017, 49)));
    }

    private EpiWeek parseSubFormKey(String subFormId) {
        return EpiWeekType.INSTANCE.fromSubFormKey(
                new RecordRef(ResourceId.valueOf("FORM"), ResourceId.valueOf(subFormId)));
    }
}