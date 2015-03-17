package org.activityinfo.test.webdriver;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.inject.Singleton;
import org.activityinfo.test.config.ConfigProperty;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class PhantomJsProvider implements WebDriverProvider {

    public static final BrowserProfile BROWSER_PROFILE = new BrowserProfile(OperatingSystem.host(),
            BrowserVendor.CHROME, "phantom.js");

    public static final ConfigProperty PHANTOM_JS_PATH = new ConfigProperty("phantomjsPath", 
            "PhantomJS binary path");
    
    private WebDriverPool pool = new WebDriverPool();

    private File logFile;
    private String logLevel = "INFO";
    private File localStoragePath;
    private File homeDir;
    
    public PhantomJsProvider() {
        pool = new WebDriverPool();
        pool.setMaxTotalSize(1);
        pool.setCreator(new Function<BrowserProfile, WebDriver>() {
            @Override
            public WebDriver apply(BrowserProfile input) {
                return createDriver();
            }
        });
    }

    public File getLogFile() {
        return logFile;
    }

    public void setLogFile(File logFile) {
        this.logFile = logFile;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public File getLocalStoragePath() {
        return localStoragePath;
    }

    public void setLocalStoragePath(File localStoragePath) {
        this.localStoragePath = localStoragePath;
    }

    public File getHomeDir() {
        return homeDir;
    }

    public void setHomeDir(File homeDir) {
        this.homeDir = homeDir;
    }

    private WebDriver createDriver() {

        PrintStream errorStream = System.err;
        
        try {

            // WebDriver dumps EVERYTHING to stderr
            System.setErr(new PrintStream(ByteStreams.nullOutputStream()));
            
            List<String> arguments = Lists.newArrayList();
            arguments.add("--webdriver-loglevel=" + logLevel);
            if (localStoragePath != null) {
                arguments.add("--local-storage-path=" + localStoragePath.getAbsolutePath());
            }

            Map<String, String> environment = new HashMap<>();
            if (homeDir != null) {
                environment.put("HOME", homeDir.getAbsolutePath());
            }

            PhantomJSDriverService service = new PhantomJSDriverService.Builder()
                    .usingPhantomJSExecutable(PhantomJsProvider.PHANTOM_JS_PATH.getFile())
                    .usingAnyFreePort()
                    .withLogFile(logFile)
                    .withEnvironment(environment)
                    .usingCommandLineArguments(arguments.toArray(new String[0]))
                    .build();


            DesiredCapabilities capabilities = new DesiredCapabilities();

            WebDriver driver = new PhantomJSDriver(service, capabilities);
            driver.manage().window().setSize(new Dimension(1400, 1000));

            return driver;
            
        } finally {
            System.setErr(errorStream);
        }
    }

    @Override
    public List<BrowserProfile> getSupportedProfiles() {
        return Collections.singletonList(BROWSER_PROFILE);
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
    public WebDriver start(String name, BrowserProfile profile) {
     //   return pool.get(BROWSER_PROFILE);
        return createDriver();
    }

}
