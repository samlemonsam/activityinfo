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

    private static final int PARTNER_COUNT = 80;
    public static final int USERS_PER_PARTNER = 10;

    private final List<Sector> sectors = Lists.newArrayList();
    private final List<PartnerOrganization> partners = Lists.newArrayList();

    public CoordinationScenario() {
        
        addSector("Health");
        addSector("Protection");
        addSector("SGBV");
        addSector("Food Security");
        addSector("Child Protection");
        addSector("Basic Assistance");
        addSector("Shelter");
        addSector("WASH");
        addSector("Education");
        addSector("Social Stability");
        addSector("Social Cohesion");
        addSector("Livelihoods");

        for(int i=0;i<PARTNER_COUNT;++i) {
            addPartner(i);
        }
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
        partners.add(new PartnerOrganization(this, PartnerOrganization.partnerName(i), USERS_PER_PARTNER));
    }

    public List<Sector> getSectors() {
        return sectors;
    }

    public List<PartnerOrganization> getPartners() {
        return partners;
    }

    @Override
    public String toString() {
        return "CoordinationScenario";
    }
}
