package org.activityinfo.test.webdriver;

import com.google.common.base.Function;
import com.google.inject.Singleton;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Runs tests against a local chrome browser.
 */
@Singleton
public class ChromeWebDriverProvider implements WebDriverProvider {
    
    
    private static final WebDriverPool POOL = initPool();
    
    private static final BrowserProfile PROFILE = new BrowserProfile(OperatingSystem.host(), BrowserVendor.CHROME);

    public ChromeWebDriverProvider() {
    }

    private static WebDriverPool initPool() {
        WebDriverPool pool = new WebDriverPool();
        //pool.setMaxTotalSize(3);
        pool.setCreator(new Function<BrowserProfile, WebDriver>() {
            @Override
            public WebDriver apply(BrowserProfile input) {
                
                // Start a local http proxy that we can use to control the 
                // the connection's properties
//                ProxyController proxyController = new ProxyController();
//                proxyController.start();

                ChromeOptions options = new ChromeOptions();
                options.addArguments("--verbose");
//                
                DesiredCapabilities capabilities = new DesiredCapabilities();
            //    capabilities.setCapability(CapabilityType.PROXY, proxyController.getWebDriverProxy());
                capabilities.setCapability(ChromeOptions.CAPABILITY, options);

                // return new ProxiedWebDriver(new ChromeDriver(capabilities), proxyController);
                return new ChromeDriver(capabilities);
            }
        });
        return pool;
    }


    @Override
    public WebDriver start(String name, BrowserProfile profile) {
       return POOL.get(PROFILE);
    }
}