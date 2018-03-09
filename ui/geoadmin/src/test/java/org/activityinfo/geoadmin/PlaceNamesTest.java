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
package org.activityinfo.geoadmin;

import org.junit.Test;

public class PlaceNamesTest {

	@Test
	public void tomasina() {

		check("TOAMASINA I", "TOAMASINA II");
		check("TOAMASINA I", "TOAMASINA I");
		check("Kindu", "Kinshasa");
		check("Kindu", "INS");
		
	
	}

    @Test
    public void zataari() {
        check("District 1", "District 12");
        check("District 1", "District 1");

    }

	private void check(String s1, String s2) {
		System.out.println(String.format("%s <=> %s => %f", s1, s2, PlaceNames.similarity(s1, s2)));
	}
}
