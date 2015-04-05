package org.activityinfo.test.steps.web;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.driver.ApplicationDriver;
import org.activityinfo.test.driver.OfflineMode;
import org.activityinfo.test.webdriver.ScreenShotLogger;
import org.activityinfo.test.webdriver.WebDriverSession;

import javax.inject.Inject;

@ScenarioScoped
public class OfflineSteps {

    @Inject
    private WebDriverSession session;

    @Inject
    private ApplicationDriver driver;


    @Inject
    private ScreenShotLogger logger;


    @Given("I have enabled offline mode$")
    public void I_have_enabled_offline_mode() {
        if(driver.getCurrentOfflineMode() != OfflineMode.OFFLINE) {
            driver.enableOfflineMode();
        }
    }


    @When("^I open the application without an internet connection$")
    public void I_open_the_application_without_an_internet_connection() throws Throwable {
        // Need to enforce this with a proxy...
        
    }

    @When("^an internet connection becomes available$")
    public void an_internet_connection_becomes_available() throws Throwable {
        // Need to enforce this with a proxy...
    }

    @And("^I synchronize with the server$")
    public void I_synchronize_with_the_server() throws Throwable {
        driver.synchronize();
    }
}
