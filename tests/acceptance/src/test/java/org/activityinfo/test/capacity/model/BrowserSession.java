package org.activityinfo.test.capacity.model;


import org.activityinfo.test.capacity.agent.Agent;
import org.activityinfo.test.driver.UiApplicationDriver;

public interface BrowserSession {

    void execute(Agent agent, UiApplicationDriver applicationDriver);
    
}
