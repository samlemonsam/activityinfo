package org.activityinfo.test.webdriver;

import com.google.common.base.Preconditions;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.BrowserType;

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
    public WebDriverSession start(DeviceProfile device) {
        Preconditions.checkArgument(supports(device));
        
        PhantomJSDriver driver = new PhantomJSDriver();
        driver.manage().window().setSize(new Dimension(1400,1000));

        return new Session(driver);
    }

    private class Session implements WebDriverSession {
        private final PhantomJSDriver driver;

        public Session(PhantomJSDriver driver) {
            this.driver = driver;
        }

        @Override
        public WebDriver getDriver() {
            return driver;
        }

        @Override
        public void finished(boolean passed) {
            driver.quit();
        }
    }
}
