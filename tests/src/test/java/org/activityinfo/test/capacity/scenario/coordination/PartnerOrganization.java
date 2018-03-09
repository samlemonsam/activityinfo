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
import org.activityinfo.test.capacity.action.Sampling;
import org.activityinfo.test.capacity.model.UserRole;

import java.util.List;

/**
 * Models a partner organization with certain number of users
 */
public class PartnerOrganization {

    private CoordinationScenario scenario;
    private String partnerName;
    private List<UserRole> users = Lists.newArrayList();
    
    public PartnerOrganization(CoordinationScenario scenario, String partnerName, int numUsers) {
        this.scenario = scenario;
        this.partnerName = partnerName;
        for(int i=0;i<numUsers;++i) {
            users.add(new ReportingUser(this, username(i), Sampling.chooseOne(scenario.getSectors())));
        }
    }

    public String getName() {
        return partnerName;
    }

    public String getDomain() {
        return partnerName.toLowerCase() + ".org";
    }

    public List<UserRole> getUsers() {
        return users;
    }
    
    private String username(int i) {
        return String.format("user%d@%s", i, getDomain());
    }

    public static String partnerName(int index) {
        char[] digits = Integer.toString(100 + index).toCharArray();
        char[] letters = new char[digits.length];
        for(int i=0;i<digits.length;++i) {
            letters[i] = (char)('A' + (digits[i] - '0'));
        }
        return new String(letters);
    }
}
