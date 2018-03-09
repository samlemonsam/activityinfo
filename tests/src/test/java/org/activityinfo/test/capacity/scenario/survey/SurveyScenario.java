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
package org.activityinfo.test.capacity.scenario.survey;

import org.activityinfo.test.capacity.model.Scenario;
import org.activityinfo.test.capacity.model.UserRole;

import java.util.List;

/**
 * Models a survey use case, where a few forms are setup and then 
 * completed by a large number of surveys in a short period of time.
 */
public class SurveyScenario implements Scenario {


    @Override
    public List<UserRole> getUsers() {
        return null;
    }

    @Override
    public int getDayCount() {
        return 0;
    }
}
