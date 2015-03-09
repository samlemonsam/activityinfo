package org.activityinfo.test.webdriver;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.List;

/**
 * Runs tests against a local chrome browser.
 */
@Singleton
public class ChromeWebDriverProvider implements WebDriverProvider {

    private WebDriverPool pool;
    
    private static final BrowserProfile PROFILE = new BrowserProfile(OperatingSystem.host(), BrowserVendor.CHROME);

    public ChromeWebDriverProvider() {
        pool = new WebDriverPool();
        pool.setMaxTotalSize(1);
        pool.setCreator(new Function<BrowserProfile, WebDriver>() {
            @Override
            public WebDriver apply(BrowserProfile input) {
                return new ChromeDriver();
            }
        });
    }

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
       return pool.get(PROFILE);
    }
}
