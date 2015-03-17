package org.activityinfo.test.capacity.scenario;

import org.activityinfo.test.capacity.agent.Agent;

import java.util.ArrayList;
import java.util.List;

/**
 * Models a partner organization with certain number of users
 */
public class Partner {

    private String partnerName;
    private List<Agent> users = new ArrayList<>();

    
    public Partner(String partnerName) {
        this.partnerName = partnerName;
    }

    public void addUser(Agent agent) {
        users.add(agent);
    }


    public String getName() {
        return partnerName;
    }

    public List<Agent> getUsers() {
        return users;
    }

    public String getDomain() {
        return partnerName.toLowerCase() + ".org";
    }
}
