package org.activityinfo.test.capacity.agent;


import com.codahale.metrics.Counter;
import com.google.common.io.Files;
import org.activityinfo.test.capacity.Metrics;
import org.activityinfo.test.capacity.model.BrowserSession;
import org.activityinfo.test.capacity.model.ScenarioContext;
import org.activityinfo.test.driver.ApiApplicationDriver;
import org.activityinfo.test.driver.UiApplicationDriver;
import org.activityinfo.test.pageobject.web.LoginPage;
import org.activityinfo.test.sut.UserAccount;
import org.activityinfo.test.webdriver.PhantomJsProvider;
import org.joda.time.LocalDate;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Agent {
    private static final Logger LOGGER = Logger.getLogger(Agent.class.getName());

    private static final AtomicInteger SCREENSHOT_INDEX = new AtomicInteger(1);
    
    private static final AtomicInteger SESSION_NUMBER = new AtomicInteger(1);
    

    private ScenarioContext scenario;
    private String name;
    private UserAccount account;
    private ApiApplicationDriver driver;
    
    public Agent(ScenarioContext scenario, String name, UserAccount account, ApiApplicationDriver driver) {
        this.scenario = scenario;
        this.name = name;
        this.account = account;
        this.driver = driver;
    }

    public ScenarioContext getScenario() {
        return scenario;
    }

    public ApiApplicationDriver getDriver() {
        return driver;
    }

    public UserAccount getAccount() {
        return account;
    }

    public String getId() {
        String email = account.getEmail();
        return email.substring(0, email.indexOf('@'));
    }
    
    public String getName() {
        return name;
    }
//    
//    public void startBrowserSession(BrowserSession session) {
//        String sessionName = "session" + SESSION_NUMBER.getAndIncrement();
//        
//        WebDriver webDriver = startWebDriver(sessionName);
//        try {
//            CONCURRENT_USERS.inc();
//            logConcurrentUsers();
//
//            LoginPage loginPage = new LoginPage(webDriver, scenario.getServer());
//            UiApplicationDriver uiDriver = new UiApplicationDriver(driver, loginPage, scenario.getAliasTable());
//            uiDriver.login(account);
//            
//            session.execute(this, uiDriver);
//            
//        } catch (Exception e) {
//            Metrics.ERRORS.inc();
//            logError(webDriver, e);
//        } finally {
//            CONCURRENT_USERS.dec();
//            logConcurrentUsers();
//            try {
//                webDriver.quit();
//            } catch(Exception e) {
//                LOGGER.log(Level.SEVERE, "Exception while shutting down web driver", e);
//            }
//        }
////    }
//


    private WebDriver startWebDriver(String sessionName) {
        
        File userDir = new File(getId());
        if(!userDir.exists()) {
            boolean succeeded = userDir.mkdirs();
            if(!succeeded) {
                throw new RuntimeException("Could not create " + userDir);
            }
        }
        
        File sessionLog = new File(userDir, new LocalDate().toString("YYYYMMDD") + ".phantomjs.log");
        if(sessionLog.exists()) {
            sessionLog.delete();
        }
        
        PhantomJsProvider provider = new PhantomJsProvider();
        provider.setLogFile(sessionLog);
        provider.setHomeDir(userDir);

        return provider.start(sessionName, PhantomJsProvider.BROWSER_PROFILE);
    }

    private void dumpLogs(WebDriver webDriver) {
        for (LogEntry logEntry : webDriver.manage().logs().get(LogType.BROWSER)) {
            LOGGER.log(logEntry.getLevel(), logEntry.getMessage());
        }
    }

    private void logError(WebDriver webDriver, Exception sessionException) {
        
        long screenshotIndex = SCREENSHOT_INDEX.getAndIncrement();
        String screenshotName = "screenshot" + screenshotIndex + ".png";

        LOGGER.log(Level.SEVERE, String.format("Exception thrown during browser session: %s [%s]",
                sessionException.getMessage(), screenshotName));

        try {
            byte[] screenshot = ((TakesScreenshot)webDriver).getScreenshotAs(OutputType.BYTES);
            File screenshotDir = new File("screenshots");
            if (!screenshotDir.exists()) {
                boolean success = screenshotDir.mkdirs();
                if(!success) {
                    throw new IOException("Failed to create screenshot dir at " + screenshotDir.getAbsolutePath());
                }
            }
            File screenshotFile = new File(screenshotDir, screenshotName);
            Files.write(screenshot, screenshotFile);
            LOGGER.log(Level.SEVERE, "Wrote screenshot to " + screenshotFile);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception writing screenshot", e);
        }
    }
}
