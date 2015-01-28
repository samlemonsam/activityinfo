package org.activityinfo.test.webdriver;

import com.google.common.base.Preconditions;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;


public class PhantomJsProvider implements WebDriverProvider {

    @Override
    public boolean supports(DeviceProfile profile) {
        if(!(profile instanceof BrowserProfile)) {
            return false;
        }
        BrowserProfile browser = (BrowserProfile) profile;
        switch(browser.getVendor()) {
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
