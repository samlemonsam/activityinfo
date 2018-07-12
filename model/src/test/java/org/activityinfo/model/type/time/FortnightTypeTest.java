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

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class FortnightTypeTest {

    @Test
    public void test() {
        assertThat(parseSubFormKey("s0417565614-2017W46-47"), equalTo(new FortnightValue(2017, 46)));
        assertThat(parseSubFormKey("s0417565614-2017W2-3"), equalTo(new FortnightValue(2017, 2)));

    }

    @Test
    public void parse() {
        assertThat(FortnightType.INSTANCE.parseString("2018W9-W10"), equalTo(new FortnightValue(2018, 9)));
    }

    private FortnightValue parseSubFormKey(String subFormId) {
        return FortnightType.INSTANCE.fromSubFormKey(
                new RecordRef(ResourceId.valueOf("FORM"), ResourceId.valueOf(subFormId)));
    }
}