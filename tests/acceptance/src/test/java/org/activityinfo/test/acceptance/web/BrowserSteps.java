package org.activityinfo.test.acceptance.web;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import cucumber.api.PendingException;
import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.webdriver.*;
import org.openqa.selenium.remote.BrowserType;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@ScenarioScoped
public class BrowserSteps {

    @Inject
    private WebDriverSession session;
    
    private Scenario scenario;

    @Before
    public void setUp(Scenario scenario) {
        this.scenario = scenario;
    }
    
    @Given("^I am using (.*) on (.*)$")
    public void I_am_using_browser_version_on_os(String browser, String os) throws Throwable {
        session.start(scenario.getName(), new BrowserProfile(parseOs(os), parseVendor(browser), extractVersion(browser)));
    }

    
    private String extractVersion(String browser) {
        Matcher matcher = Pattern.compile("[\\d\\\\.]+").matcher(browser);
        if(matcher.find()) {
            return matcher.group(0);
        } else {
            return Version.UNKNOWN.toString();
        }
    }

    private BrowserVendor parseVendor(String browser) {
        browser = browser.toLowerCase();
        if(browser.contains("ie") || browser.contains("internet")) {
            return BrowserVendor.IE;
        } else {
            for(BrowserVendor vendor : BrowserVendor.values()) {
                if(browser.contains(vendor.name().toLowerCase())) {
                    return vendor;
                }
            }
        }
        throw new IllegalArgumentException("Could not parse browser type. Should be one of " + 
                Arrays.toString(BrowserVendor.values()));
    }

    private OperatingSystem parseOs(String os) {
        os = os.toLowerCase();
        if(os.contains("win")) {
            if(os.contains("xp")) {
                return new OperatingSystem(OperatingSystemType.WINDOWS, "XP");
            } else {
                return OperatingSystemType.WINDOWS.version(extractVersion(os));
            }
        } else if(os.contains("linux")) {
            return OperatingSystemType.LINUX.unknownVersion();
        }
        throw new IllegalArgumentException(os);
    }

}