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
package org.activityinfo.test.capacity.scripts;

import org.activityinfo.test.capacity.model.Scenario;
import org.activityinfo.test.capacity.scenario.coordination.CoordinationScenario;

import java.util.Arrays;
import java.util.Collection;

/**
 * Simple scenario for testing the test :-)
 */
public class SingleDatabase implements CapacityTestScript {
    @Override
    public Collection<Scenario> get() {
        int databaseCount = 1;
        int partnerCount = 1;
        int usersPerPartner = 1;
        return Arrays.<Scenario>asList(new CoordinationScenario("Test", databaseCount, partnerCount, usersPerPartner));
    }
}
