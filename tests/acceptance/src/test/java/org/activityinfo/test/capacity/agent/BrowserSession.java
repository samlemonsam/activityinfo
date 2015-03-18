package org.activityinfo.test.capacity.agent;


import org.activityinfo.test.driver.UiApplicationDriver;

public interface BrowserSession {

    void execute(Agent agent, UiApplicationDriver applicationDriver);
    
}
