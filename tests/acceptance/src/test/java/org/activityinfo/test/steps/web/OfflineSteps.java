package org.activityinfo.test.steps.web;

import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.driver.ApplicationDriver;
import org.activityinfo.test.driver.UiApplicationDriver;
import org.activityinfo.test.webdriver.ScreenShotLogger;
import org.activityinfo.test.pageobject.api.PageBinder;
import org.activityinfo.test.pageobject.web.ApplicationPage;
import org.activityinfo.test.pageobject.web.OfflineMode;
import org.activityinfo.test.webdriver.*;

import javax.inject.Inject;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@ScenarioScoped
public class OfflineSteps {

    @Inject
    private WebDriverSession session;

    @Inject
    private PageBinder pages;
    
    @Inject
    private UiApplicationDriver driver;


    @Inject
    private ScreenShotLogger logger;


    @Given("^offline mode is not enabled$")
    public void offline_mode_is_not_enabled() throws Throwable {
        ApplicationPage page = pages.waitFor(ApplicationPage.class);

        assertThat(page.getOfflineMode(), equalTo(OfflineMode.ONLINE));
    }


    @When("^I enable offline mode$")
    public void I_enable_offline_mode() throws Throwable {
        driver.ensureLoggedIn();
        pages.waitFor(ApplicationPage.class)
                .openSettingsMenu()
                .enableOfflineMode();

        logger.snapshot();
    }

    @Then("^I should be working offline$")
    public void I_should_be_working_offline() throws Throwable {
        // Express the Regexp above with the code you wish you had
        throw new PendingException();
    }

    @Then("^offline mode should be enabled$")
    public void offline_mode_should_be_enabled() throws Throwable {
        ApplicationPage page = pages.waitFor(ApplicationPage.class);
        page.assertOfflineModeLoads();
    }


    @Then("^synchronization should complete successfully$")
    public void synchronization_should_complete_successfully() throws Throwable {
        ApplicationPage page = pages.waitFor(ApplicationPage.class);
        page.assertOfflineModeLoads();        
    }

}
