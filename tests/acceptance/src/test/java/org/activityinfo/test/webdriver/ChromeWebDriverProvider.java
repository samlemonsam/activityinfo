package org.activityinfo.test.webdriver;

import com.google.common.collect.Lists;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.List;

/**
 * Runs tests against a local chrome browser.
 */
public class ChromeWebDriverProvider implements WebDriverProvider {
    @Override
    public List<? extends DeviceProfile> getSupportedProfiles() {
        return Lists.newArrayList();
    }

    @Override
    public boolean supports(DeviceProfile profile) {
        return true;
    }

    @Override
    public WebDriver start(String name, BrowserProfile profile) {
        WebDriver driver = new ChromeDriver();
        return driver;
    }
}
