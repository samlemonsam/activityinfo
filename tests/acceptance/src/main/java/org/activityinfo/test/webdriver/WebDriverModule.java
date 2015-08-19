package org.activityinfo.test.webdriver;

import com.google.common.base.Strings;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.driver.ApplicationDriver;
import org.activityinfo.test.driver.UiApplicationDriver;
import org.openqa.selenium.WebDriver;


public class WebDriverModule extends AbstractModule {
    
    private String type;
    

    public WebDriverModule(String type) {
        if(!Strings.isNullOrEmpty(type)) {
            this.type = type;
        } else if(SauceLabsDriverProvider.isEnabled()) {
            this.type = "sauce";
        } else {
            this.type = "chrome";
        }
    }
    
    public WebDriverModule() {
        this(null);
    }
    
    @Override
    protected void configure() {
        
        bind(ApplicationDriver.class).to(UiApplicationDriver.class);

        switch (type) {
            case "phantomjs":
                System.out.println("Using PhantomJS as WebDriver");
                bind(WebDriverProvider.class).to(PhantomJsProvider.class);
                bind(SessionReporter.class).to(SimpleReporter.class);
                break;
            case "chrome":
                bind(WebDriverProvider.class).to(ChromeWebDriverProvider.class);
                bind(SessionReporter.class).to(SimpleReporter.class);
                break;
            case "sauce":
                bind(WebDriverProvider.class).to(SauceLabsDriverProvider.class);
                bind(SessionReporter.class).to(SauceReporter.class);
                break;
            default:
                throw new IllegalStateException("Invalid type: " + type);
        }
   
    }

    @ScenarioScoped 
    @Provides
    public WebDriver provideDriver(WebDriverSession session) {
        return session.getDriver();
    }
}
