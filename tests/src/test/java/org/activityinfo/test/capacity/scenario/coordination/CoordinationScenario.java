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
import org.activityinfo.test.capacity.model.Scenario;
import org.activityinfo.test.capacity.model.UserRole;

import java.util.List;

/**
 * Models a country-wide humanitarian response that involves
 * thousands of users across several sectors with very spikey reporting deadlines.
 */
public class CoordinationScenario implements Scenario {

    public static final String COORDINATING_AGENCY = "UNHCR";

    public static final String[] SECTORS = { 
            "Health", "Protection", "SGBV", "Food Security", "Child Protection",
            "Basic Assistance", "Shelter", "WASH", "Education", "Social Stability", "Social Cohesion"};
    
    private final int partnerCount;
    private final int usersPerPartner;
    private String name;
    private final int databaseCount;

    
    private final List<Sector> sectors = Lists.newArrayList();
    private final List<PartnerOrganization> partners = Lists.newArrayList();

    public CoordinationScenario(String name, int databaseCount, int partnerCount, int usersPerPartner) {
        this.name = name;
        this.databaseCount = databaseCount;
        this.partnerCount = partnerCount;
        this.usersPerPartner = usersPerPartner;
        
        for(int i=0;i<Math.min(this.databaseCount, SECTORS.length);++i) {
            addSector(SECTORS[i]);
        }

        for(int i=0;i< this.partnerCount;++i) {
            addPartner(i);
        }
    }

    public String getName() {
        return name;
    }
    
    public List<UserRole> getUsers() {
        List<UserRole> users = Lists.newArrayList();
        for(Sector sector : sectors) {
            users.add(sector.getSectorLead());
        }
        for(PartnerOrganization partner : partners) {
            users.addAll(partner.getUsers());
        }
        return users;
    }

    @Override
    public int getDayCount() {
        return 10;
    }

    private void addSector(String sectorName) {
        sectors.add(new Sector(this, sectorName));
    }

    private void addPartner(int i) {
        partners.add(new PartnerOrganization(this, PartnerOrganization.partnerName(i), usersPerPartner));
    }

    public List<Sector> getSectors() {
        return sectors;
    }

    public List<PartnerOrganization> getPartners() {
        return partners;
    }

    @Override
    public String toString() {
        return name + " Coordination Scenario";
    }


}
