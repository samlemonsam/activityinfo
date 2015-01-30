package org.activityinfo.test.webdriver;

import com.google.common.base.Preconditions;
import cucumber.api.Scenario;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Collections;
import java.util.List;


public class PhantomJsProvider implements WebDriverProvider {

    @Override
    public List<BrowserProfile> getSupportedProfiles() {
        return Collections.singletonList(new BrowserProfile(OperatingSystem.host(), BrowserVendor.CHROME, "phantom.js"));
    }

    @Override
    public boolean supports(DeviceProfile profile) {
        if(!(profile instanceof BrowserProfile)) {
            return false;
        }
        BrowserProfile browser = (BrowserProfile) profile;
        switch(browser.getType()) {
            case CHROME:
            case SAFARI:
                return true;
            default:
                return false;
        }
    }

    @Override
    public WebDriver start(String browserType, String browserVersion, String platform) {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        WebDriver driver = new PhantomJSDriver();
        driver.manage().window().setSize(new Dimension(1400,1000));

        return driver;
    }

}
