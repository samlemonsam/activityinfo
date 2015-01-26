package org.activityinfo.test.harness;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.saucelabs.common.Utils;
import com.saucelabs.saucerest.SauceREST;
import cucumber.api.Scenario;
import org.activityinfo.test.config.ConfigProperty;
import org.activityinfo.test.config.ConfigurationError;
import org.activityinfo.test.device.BrowserVendor;
import org.activityinfo.test.device.DeviceProfile;
import org.activityinfo.test.device.OperatingSystem;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class SauceLabs extends AbstractModule {

    public static final ConfigProperty SAUCE_USERNAME = new ConfigProperty("sauce.username", "Sauce.io username");
    public static final ConfigProperty SAUCE_ACCESS_KEY = new ConfigProperty("sauce.accessKey", "Sauce.io access key");

    @Override
    protected void configure() {
        bind(TestReporter.class).to(SauceReporter.class).in(Singleton.class);
        bind(DeviceProfile.class).toInstance(
                new DeviceProfile(OperatingSystem.WINDOWS_8, BrowserVendor.INTERNET_EXPLORER, "11"));
    }

    @Provides
    @Singleton
    public RemoteWebDriver provideRemoteWebDriver(DeviceProfile device) {

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(CapabilityType.BROWSER_NAME, device.getBrowser().sauceId());
        capabilities.setCapability(CapabilityType.PLATFORM, device.getOS().sauceId());
        capabilities.setCapability("name", "Test");
        try {
            return new RemoteWebDriver(
                    new URL("http://" + SAUCE_USERNAME.get() + ":" + SAUCE_ACCESS_KEY.get() + "@ondemand.saucelabs.com:80/wd/hub"),
                    capabilities);
        } catch (MalformedURLException e) {
            throw new ConfigurationError("Malformed sauce labs URL", e);
        }
    }

    @Provides
    @Singleton
    public WebDriver provideWebDriver(RemoteWebDriver webDriver) {
        return webDriver;
    }

    public static class SauceReporter implements TestReporter {

        private RemoteWebDriver driver;
        private SauceREST sauceREST;
        private SessionId sessionId;

        @Inject
        public SauceReporter(RemoteWebDriver driver) {
            this.driver = driver;
            this.sessionId = driver.getSessionId();
        }

        @Override
        public void testStarting() {
            this.sauceREST = new SauceREST(SAUCE_USERNAME.get(), SAUCE_ACCESS_KEY.get());

        }

        @Override
        public void testFinished(Scenario scenario) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("passed", !scenario.isFailed());

            Utils.addBuildNumberToUpdate(updates);

            this.sauceREST.updateJobInfo(sessionId.toString(), updates);

            scenario.write("Sauce URL: " + this.sauceREST.getPublicJobLink(sessionId.toString()));
        }
    }
}
