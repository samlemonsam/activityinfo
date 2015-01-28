package org.activityinfo.test.webdriver;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import cucumber.api.Profile;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.openqa.selenium.WebDriver;


public class WebDriverProfileModule extends AbstractModule {

    private WebDriverProvider webDriverProvider;
    private final DeviceProfile deviceProfile;

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
        return webDriverProvider.start(deviceProfile);
    }
    
    @Provides
    @ScenarioScoped
    public WebDriver provideWebDriver(WebDriverSession session) {
        return session.getDriver();
    }
}
