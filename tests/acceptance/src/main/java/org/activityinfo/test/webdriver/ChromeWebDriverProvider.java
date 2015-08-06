package org.activityinfo.test.webdriver;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.util.Map;

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

                Map<String, String> environment = Maps.newHashMap();
                if(Strings.isNullOrEmpty(System.getProperty("browserTimezone"))) {
                    environment.put("TZ", "America/New_York");
                }

                ChromeDriverService service = new ChromeDriverService.Builder()
                        .usingAnyFreePort()
                        .usingDriverExecutable(findDriverBin())
                        .withEnvironment(environment)
                        .build();
                
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--verbose");
//                
                DesiredCapabilities capabilities = new DesiredCapabilities();
            //    capabilities.setCapability(CapabilityType.PROXY, proxyController.getWebDriverProxy());
                capabilities.setCapability(ChromeOptions.CAPABILITY, options);

                // return new ProxiedWebDriver(new ChromeDriver(capabilities), proxyController);
                return new ChromeDriver(service, capabilities);
            }
        });
        return pool;
    }

    private static File findDriverBin() {
       
        // check path
        String[] paths = System.getenv("PATH").split(File.pathSeparator);
        for (String path : paths) {
            File bin = new File(path + File.separator + "chromedriver");
            if(bin.exists()) {
                return bin;
            }
            File exe = new File(path + File.separator + "chromedriver.exe");
            if(exe.exists()) {
                return exe;
            }
        }
        throw new IllegalStateException("Could not find chromedriver binary in PATH");
    }



    @Override
    public WebDriver start(String name, BrowserProfile profile) {
       return POOL.get(PROFILE);
    }
}
