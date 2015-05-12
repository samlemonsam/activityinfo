package org.activityinfo.test.ui;

import com.google.inject.Guice;
import com.google.inject.Injector;
import cucumber.runtime.java.guice.impl.ScenarioModule;
import cucumber.runtime.java.guice.impl.SequentialScenarioScope;
import org.activityinfo.test.driver.AliasTable;
import org.activityinfo.test.driver.ApiApplicationDriver;
import org.activityinfo.test.driver.DriverModule;
import org.activityinfo.test.driver.UiApplicationDriver;
import org.activityinfo.test.pageobject.web.ApplicationPage;
import org.activityinfo.test.pageobject.web.LoginPage;
import org.activityinfo.test.sut.DevServerAccounts;
import org.activityinfo.test.sut.SystemUnderTest;
import org.activityinfo.test.sut.UserAccount;
import org.activityinfo.test.webdriver.WebDriverModule;
import org.activityinfo.test.webdriver.WebDriverSession;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class UiDriver extends TestWatcher {

    private final SequentialScenarioScope scenarioScope;
    private final Injector injector;

    private ApplicationPage applicationPage;
    private UserAccount currentUser;
    
    public UiDriver() {
        scenarioScope = new SequentialScenarioScope();
        injector = Guice.createInjector(
                new SystemUnderTest(),
                new WebDriverModule(),
                new ScenarioModule(scenarioScope),
                new DriverModule("web"));        
        
    }

    @Override
    protected void starting(Description description) {
        scenarioScope.enterScope();
    }


    @Override
    protected void finished(Description description) {
        shutdownWebDriver();
        scenarioScope.exitScope();
    }

    private void shutdownWebDriver() {
        WebDriverSession session = injector.getInstance(WebDriverSession.class);
        if(session.isRunning()) {
            session.stop();
        }
    }

    public UserAccount anyAccount() {
        return injector.getInstance(DevServerAccounts.class).any();
    }
    
    public UiApplicationDriver ui() {
        return injector.getInstance(UiApplicationDriver.class);
    }
    
    public ApiApplicationDriver setup() {
        return (ApiApplicationDriver) ui().setup();
    }

    public void loginAsAny() {
        currentUser = anyAccount();
        ui().login(currentUser);
    }
    
    public ApplicationPage applicationPage() {
        if(applicationPage == null) {
            applicationPage = injector.getInstance(LoginPage.class)
                    .navigateTo().loginAs(currentUser).andExpectSuccess();
        }
        return applicationPage;
    }

    public String alias(String testHandle) {
        return injector.getInstance(AliasTable.class).getAlias(testHandle);
    }
}
