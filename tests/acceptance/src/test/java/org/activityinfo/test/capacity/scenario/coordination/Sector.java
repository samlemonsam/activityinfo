package org.activityinfo.test.capacity.scenario.coordination;


import org.activityinfo.test.capacity.model.UserRole;

public class Sector {
    private CoordinationScenario response;
    private String sectorName;
    
    private int activityCount = 35;
    private int indicatorCount = 25;
    
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

    public int getIndicatorCount() {
        return indicatorCount;
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
