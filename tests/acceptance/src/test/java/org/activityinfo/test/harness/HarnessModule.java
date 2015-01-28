package org.activityinfo.test.harness;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.activityinfo.test.config.ConfigProperty;
import org.activityinfo.test.webdriver.BrowserProfile;
import org.activityinfo.test.sut.Accounts;
import org.activityinfo.test.sut.Server;
import org.activityinfo.test.webdriver.WebDriverSession;
import org.openqa.selenium.WebDriver;

import static org.activityinfo.test.sut.PredefinedAccounts.fromResource;


public class HarnessModule extends AbstractModule {
    
    
    
    private ThreadSafeScenarioScope testScope;

    public HarnessModule(ThreadSafeScenarioScope testScope) {
        this.testScope = testScope;
    }

    @Override
    protected void configure() {

        
        // make our scope instance injectable
        bind(ThreadSafeScenarioScope.class).toInstance(testScope);
    }
    
}
