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
package org.activityinfo.test.capacity.scenario.coordination;

import com.google.common.collect.Lists;
import org.activityinfo.test.capacity.model.UserRole;

import java.util.List;

public class Sector {
    private CoordinationScenario response;
    private String sectorName;
    
    private int activityCount = 35;
    private int indicatorCount = 40;
    
    private SectorLead sectorLead;

    public Sector(CoordinationScenario response, String sectorName) {
        this.response = response;
        this.sectorName = sectorName;
        this.sectorLead = new SectorLead(this);
    }
    
    public String getDatabaseName() {
        return sectorName;
    }
    
    public String getSectorName() {
        return sectorName;
    }

    public int getActivityCount() {
        return activityCount;
    }

    public String getActivityFormName(int i) {
        return String.format("%s Activity %d", sectorName, i);
    }
    
    public List<String> getActivityForms() {
        List<String> forms = Lists.newArrayList();
        for(int i=0;i<activityCount;++i) {
            forms.add(getActivityFormName(i));
        }
        return forms;
    }
    
    public int getIndicatorCount() {
        return indicatorCount;
    }


    public String getIndicatorName(String form, int indicatorIndex) {
        return String.format("%s Indicator %d", form, indicatorIndex);
    }

    public UserRole getSectorLead() {
        return sectorLead;
    }

    public CoordinationScenario getScenario() {
        return response;
    }

    @Override
    public String toString() {
        return sectorName;
    }

}
