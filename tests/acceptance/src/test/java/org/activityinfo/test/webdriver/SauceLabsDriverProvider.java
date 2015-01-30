package org.activityinfo.test.webdriver;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.saucelabs.common.Utils;
import com.saucelabs.saucerest.SauceREST;
import cucumber.api.Scenario;
import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.JsonParser;
import gherkin.deps.com.google.gson.annotations.SerializedName;
import org.activityinfo.test.config.ConfigProperty;
import org.activityinfo.test.config.ConfigurationError;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Singleton
public class SauceLabsDriverProvider implements WebDriverProvider {

    public static final String JENKINS_SAUCE_USER_NAME = "SAUCE_USER_NAME";
    public static final String JENKINS_SAUCE_API_KEY = "SAUCE_API_KEY";
    public static final String JENKINS_STARTING_URL = "SELENIUM_STARTING_URL";

    public static final ConfigProperty SAUCE_USERNAME = new ConfigProperty("sauce.username", "Sauce.io username");
    public static final ConfigProperty SAUCE_ACCESS_KEY = new ConfigProperty("sauce.accessKey", "Sauce.io access key");
    

    private String userName;
    private String apiKey;
    

    public static boolean isEnabled() {
        return !Strings.isNullOrEmpty(System.getenv(JENKINS_SAUCE_USER_NAME)) ||
                SAUCE_USERNAME.isPresent();
    }

    @Inject
    public SauceLabsDriverProvider() {

        // Provided by the Jenkins plugin
        userName = System.getenv(JENKINS_SAUCE_USER_NAME);
        apiKey = System.getenv(JENKINS_SAUCE_API_KEY);

        if(Strings.isNullOrEmpty(userName) ||
           Strings.isNullOrEmpty(apiKey)) {

            userName = SAUCE_USERNAME.get();
            apiKey = SAUCE_ACCESS_KEY.get();
        }
    }
    
    private URL getRemoteAddress() {

        String startingUrl = System.getenv(JENKINS_STARTING_URL);
        if (Strings.isNullOrEmpty(startingUrl)) {
            startingUrl = String.format("http://%s:%s@ondemand.saucelabs.com:80/wd/hub", userName, apiKey);
        }

        try {
            return new URL(startingUrl);
        } catch (MalformedURLException e) {
            throw new ConfigurationError(String.format("Sauce labs remote address [%s] is malformed.", startingUrl), e);
        }
    }

    @Override
    public List<BrowserProfile> getSupportedProfiles() {
        try {
            return SaucePlatforms.fetchBrowsers();
        } catch (IOException e) {
            throw new RuntimeException("Could not fetch supported browser", e);
        }
    }

    @Override
    public boolean supports(DeviceProfile profile) {
        return profile instanceof BrowserProfile;
    }

    private String osName(BrowserProfile browser) {
        switch(browser.getOS().getType()) {
            case WINDOWS:
                return "Windows " + browser.getOS().getVersion();
            case OSX:
                return "OS X " + browser.getOS().getVersion();
            case LINUX:
                return "Linux";
            default:
                return null;
        }
    }

    @Override
    public WebDriver start(String browserType, String browserVersion, String platform) {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        if(browserType != null) {
            capabilities.setCapability(CapabilityType.BROWSER_NAME, browserType);
        }
        capabilities.setCapability(CapabilityType.VERSION, browserVersion);
        capabilities.setCapability(CapabilityType.PLATFORM, platform);
        return new RemoteWebDriver(getRemoteAddress(), capabilities);
    }


    public SauceREST getRestClient() {
        return new SauceREST(userName, apiKey);
    }
}
