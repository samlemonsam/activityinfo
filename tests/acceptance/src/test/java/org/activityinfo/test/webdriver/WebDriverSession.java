package org.activityinfo.test.webdriver;


import com.google.common.base.Preconditions;
import cucumber.api.Scenario;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;

import javax.inject.Inject;
import java.lang.reflect.*;

/**
 * Stores state related to an individual WebDriver session, lazily creating a WebDriver instance
 * if actually used
 */
@ScenarioScoped
public class WebDriverSession {

    private final WebDriverProvider provider;
    private WebDriver driver;
    private WebDriver proxy;
    private String testName;
    private BrowserProfile browserProfile = new BrowserProfile(OperatingSystem.WINDOWS_7, BrowserVendor.CHROME);

    @Inject
    public WebDriverSession(WebDriverProvider provider, ProxyController proxyController) {
        this.provider = provider;
        this.proxy =  (WebDriver) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{ WebDriver.class, TakesScreenshot.class, HasInputDevices.class },
                new WebDriverProxy());
    }

    public void beforeTest(String testName) {
        this.testName = testName;
    }

    private void startSession() {
        this.driver = provider.start(testName, browserProfile);
    }

    public WebDriver getDriver() {
        return proxy;
    }

    public SessionId getSessionId() {
        Preconditions.checkState(driver != null, "WebDriver is not started");
        RemoteWebDriver remoteWebDriver = (RemoteWebDriver) driver;
        return remoteWebDriver.getSessionId();
    }
    
    public boolean isRunning() {
        return driver != null;
    }

    public void stop() {
        if(driver != null) {
            driver.quit();
            driver = null;
        }
    }

    public void setBrowserProfile(BrowserProfile browserProfile) {
        this.browserProfile = browserProfile;
    }

    public BrowserProfile getBrowserProfile() {
        return browserProfile;
    }

    private class WebDriverProxy implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if(driver == null) {
                startSession();
            }
            try {
                return method.invoke(driver, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();                            
            }
        }

    }

}
