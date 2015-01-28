package org.activityinfo.test.acceptance;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.webdriver.WebDriverSession;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;


@ScenarioScoped
public class WebDriverHooks {


    @Inject
    private WebDriverSession session;
    
    @Inject
    private WebDriver webDriver;

    @After
    public void after(Scenario scenario) {
        if(webDriver instanceof TakesScreenshot) {
            byte[] screenshot = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
            scenario.embed(screenshot, "image/png");
        }

        session.finished(scenario);
    }
}
