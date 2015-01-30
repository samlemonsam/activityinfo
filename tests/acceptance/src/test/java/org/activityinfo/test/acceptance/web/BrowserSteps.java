package org.activityinfo.test.acceptance.web;

import cucumber.api.PendingException;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import org.activityinfo.test.webdriver.BrowserVendor;
import org.activityinfo.test.webdriver.OperatingSystem;
import org.activityinfo.test.webdriver.WebDriverSession;
import org.openqa.selenium.remote.BrowserType;

import javax.inject.Inject;

/**
 * Created by alex on 1/30/15.
 */
public class BrowserSteps {

    @Inject
    private WebDriverSession session;

    public static class Browser {
        String browser;
        String version;
        String os;
    }

    @Given("^I am using (.*) (.*) on (.*)$")
    public void I_am_using_browser_version_on_os(BrowserVendor browser, String version, String os) throws Throwable {
        session.start(browser.sauceId(), version, os);
    }
}