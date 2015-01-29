package org.activityinfo.test.webdriver;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import cucumber.api.Profile;
import cucumber.api.Scenario;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.openqa.selenium.WebDriver;

import java.util.concurrent.Semaphore;


public class WebDriverProfileModule extends AbstractModule {

    private WebDriverProvider webDriverProvider;
    private final DeviceProfile deviceProfile;
    
    // We only have three VMS at at time
    private Semaphore semaphore = new Semaphore(3);

    public WebDriverProfileModule(WebDriverProvider webDriverProvider, DeviceProfile deviceProfile) {
        this.webDriverProvider = webDriverProvider;
        this.deviceProfile = deviceProfile;
    }

    @Override
    protected void configure() {
        bind(Profile.class).toInstance(deviceProfile);
        bind(DeviceProfile.class).toInstance(deviceProfile);
        bind(BrowserProfile.class).toInstance((BrowserProfile) deviceProfile);
    }
    
    @Provides
    @ScenarioScoped
    public WebDriverSession provideSession() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while waiting for a Sauce permit", e);
        }
        try {
            return webDriverProvider.start(deviceProfile);
        } finally {
            semaphore.release();
        }
    }
    
    @Provides
    @ScenarioScoped
    public WebDriver provideWebDriver(WebDriverSession session) {
        return session.getDriver();
    }
}
