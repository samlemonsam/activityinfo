package org.activityinfo.test.webdriver;


import cucumber.api.Scenario;
import org.openqa.selenium.WebDriver;

public interface WebDriverSession {
    
    public WebDriver getDriver();
    
    public void finished(Scenario scenario);
}
