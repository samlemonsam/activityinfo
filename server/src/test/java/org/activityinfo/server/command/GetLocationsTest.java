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
package org.activityinfo.server.command;

import org.activityinfo.legacy.shared.command.GetLocations;
import org.activityinfo.legacy.shared.model.LocationDTO;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.server.database.OnDataSet;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/sites-simple1.db.xml")
public class GetLocationsTest extends CommandTestCase2 {

    @Test
    public void testGetLocation() {
        setUser(1);

        LocationDTO location = execute(new GetLocations(1)).getData().get(0);

        assertThat(location, notNullValue());
        assertThat(location.getName(), equalTo("Penekusu Kivu"));
        assertThat(location.getAxe(), nullValue());
        assertThat(location.getAdminEntity(1).getName(), equalTo("Sud Kivu"));
        assertThat(location.getAdminEntity(2).getName(), equalTo("Shabunda"));
    }


}
