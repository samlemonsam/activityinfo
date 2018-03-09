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

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class GetSyncRegionUpdatesTest {

    @Test
    public void paths() {
        GetSyncRegionUpdates request = new GetSyncRegionUpdates("db/4", "16901");
        
        assertThat(request.getRegionPath(), equalTo("db/4"));
        assertThat(request.getRegionType(), equalTo("db"));
        assertThat(request.getRegionId(), equalTo(4));
        
        assertThat(request.getLocalVersion(), equalTo("16901"));
        assertThat(request.getLocalVersionNumber(), equalTo(16901L));
    }
    
    @Test
    public void singleton() {
        GetSyncRegionUpdates request = new GetSyncRegionUpdates("tables", "3");

        assertThat(request.getRegionPath(), equalTo("tables"));
        assertThat(request.getRegionType(), equalTo("tables"));
    }
    
}