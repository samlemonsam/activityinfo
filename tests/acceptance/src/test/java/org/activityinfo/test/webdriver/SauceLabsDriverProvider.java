package org.activityinfo.test.webdriver;

import com.google.common.base.Charsets;
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
import org.openqa.selenium.WebDriver;
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

    @Override
    public WebDriverSession start(DeviceProfile device) {
        if(!(device instanceof BrowserProfile)) {
            throw new UnsupportedOperationException("Only supports " + BrowserProfile.class.getName());
        }
        BrowserProfile browser = (BrowserProfile) device;
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(CapabilityType.BROWSER_NAME, browser.getType().sauceId());
        capabilities.setCapability(CapabilityType.PLATFORM, osName(browser));

        return new Session(new RemoteWebDriver(getRemoteAddress(), capabilities));
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

    public class Session implements WebDriverSession {

        private RemoteWebDriver driver;
        private String sessionId;

        /**
         * The name of the Build Job, if we are running under Jenkins
         */
        private String buildJobName;

        @Inject
        public Session(RemoteWebDriver driver) {
            this.driver = driver;
            this.sessionId = driver.getSessionId().toString();

            System.out.println(String.format("Session %s starts on thread %s", sessionId, Thread.currentThread().getName()));


            buildJobName = Strings.emptyToNull(System.getenv("JOB_NAME"));
            if(buildJobName != null) {
                // Output information used by the Jenkins Sauce Labs Plugin
                // See https://wiki.jenkins-ci.org/display/JENKINS/Sauce+OnDemand+Plugin
                System.out.println(String.format("SauceOnDemandSessionID=%s job-name=%s", sessionId, buildJobName));
            }
        }
   
        @Override
        public WebDriver getDriver() {
            return driver;
        }

        @Override
        public void finished(Scenario scenario) {
            
            SauceREST sauceClient = new SauceREST(userName, apiKey);
            Map<String, Object> updates = new HashMap<>();
            updates.put("passed", !scenario.isFailed());
            updates.put("name", scenario.getName());

            Utils.addBuildNumberToUpdate(updates);

            sauceClient.updateJobInfo(sessionId, updates);

            System.out.println(String.format("Session %s for scenario %s finishes on thread %s", sessionId, scenario.getId(), Thread.currentThread().getName()));
        
            driver.quit();
        }
    }
    
}
