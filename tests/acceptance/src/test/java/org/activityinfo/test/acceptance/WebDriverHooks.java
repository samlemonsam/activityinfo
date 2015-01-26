package org.activityinfo.test.acceptance;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import org.activityinfo.test.harness.TestReporter;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;

public class WebDriverHooks {

    @Inject
    private WebDriver webDriver;

    @Inject
    private TestReporter reporter;

    @Before
    public void starting() {
        if(reporter != null) {
            reporter.testStarting();
        }
    }

    @After
    public void after(Scenario scenario) {
        if(webDriver instanceof TakesScreenshot) {
            byte[] screenshot = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
            scenario.embed(screenshot, "image/png");
        }

        if(reporter != null) {
            reporter.testFinished(scenario);
        }

        webDriver.quit();
    }
}
