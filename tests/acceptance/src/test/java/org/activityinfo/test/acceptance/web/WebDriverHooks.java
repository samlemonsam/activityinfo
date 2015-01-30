package org.activityinfo.test.acceptance.web;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.harness.ScreenShotLogger;
import org.activityinfo.test.webdriver.*;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;
import java.util.Collection;


@ScenarioScoped
public class WebDriverHooks {


    @Inject
    private WebDriverSession session;
    @Inject
    private SauceReporter reporter;

    @Inject
    private ScreenShotLogger logger;

    @Before("@web")
    public void before(Scenario scenario) {
        session.start(scenario.getName());
    }

    public void after(Scenario scenario) {
        if(session.isRunning()) {
            System.out.println(String.format("Scenario %s finishes on thread %s", scenario.getId(), Thread.currentThread().getName()));

            logger.snapshot();
            session.finished(scenario);
            reporter.finished(scenario);
        }
    }
}
