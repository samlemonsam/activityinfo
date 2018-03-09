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
package org.activityinfo.io.match.names;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class LatinPlaceNameTest {

    private LatinPlaceName name;

    @Before
    public void setUp() {
        name = new LatinPlaceName();
    }

    @Test
    public void partsAreProperlyProcessed() {
        check("Aïn-Jraïne", asList("AIN", "JRAINE"));
        check("Zouk-El-Hosmieh et Dahr Ayasse", asList("ZOUK", "EL", "HOSMIEH", "ET", "DAHR", "AYASSE"));
        check("Zouk el Moukachérine", asList("ZOUK", "EL", "MOUKACHERINE"));
        check("Mazraat Louzid (Louayziyé)", asList("MAZRAAT", "LOUZID", "LOUAYZIYE"));
    }

    private void check(String input, List<String> expectedParts) {

        name.set(input);

        System.out.println(input + " => " + name);

        assertThat("partCount", name.partCount(), equalTo(expectedParts.size()));
        for(int i=0;i!=name.partCount();i++) {
            assertThat("part " + i, name.part(i), equalTo(expectedParts.get(i)));
        }
    }
}
