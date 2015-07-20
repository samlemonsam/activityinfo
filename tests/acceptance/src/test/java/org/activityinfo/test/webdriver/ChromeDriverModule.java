package org.activityinfo.test.webdriver;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.driver.ApplicationDriver;
import org.activityinfo.test.driver.UiApplicationDriver;
import org.openqa.selenium.WebDriver;


public class ChromeDriverModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ApplicationDriver.class).to(UiApplicationDriver.class);
        bind(WebDriverProvider.class).to(ChromeWebDriverProvider.class);
        bind(SessionReporter.class).to(SimpleReporter.class);
    }

    @ScenarioScoped
    @Provides
    public WebDriver provideDriver(WebDriverSession session) {
        return session.getDriver();
    }
}
