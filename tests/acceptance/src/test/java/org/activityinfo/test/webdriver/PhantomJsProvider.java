package org.activityinfo.test.webdriver;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

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
    public WebDriver start(String testName, BrowserProfile profile) {
        WebDriver driver = new PhantomJSDriver();
        driver.manage().window().setSize(new Dimension(1400,1000));

        return driver;
    }

}
