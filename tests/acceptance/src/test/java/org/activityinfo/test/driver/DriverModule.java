package org.activityinfo.test.driver;

import com.google.inject.AbstractModule;
import cucumber.runtime.java.guice.ScenarioScoped;
import io.appium.java_client.AppiumDriver;
import org.activityinfo.test.config.ConfigurationError;
import org.activityinfo.test.driver.mail.EmailDriver;
import org.activityinfo.test.driver.mail.mailinator.MailinatorClient;
import org.activityinfo.test.driver.mail.postmark.PostmarkStubClient;
import org.activityinfo.test.driver.mail.postmark.PostmarkStubServer;
import org.activityinfo.test.odk.AndroidDevice;
import org.activityinfo.test.pageobject.odk.OdkVersion;
import org.activityinfo.test.webdriver.LocalAppiumProvider;
import org.activityinfo.test.webdriver.SauceLabsAppiumProvider;
import org.activityinfo.test.webdriver.SauceLabsDriverProvider;

public class DriverModule extends AbstractModule {

    private final String driver;

    public DriverModule() {
        driver = System.getProperty("app.driver", "web");
    }

    public DriverModule(String driver) {
        this.driver = driver;
    }

    @Override
    protected void configure() {
        switch (driver) {
            case "api":
                bind(ApplicationDriver.class).to(ApiApplicationDriver.class);
                break;
            case "web":
                bind(ApplicationDriver.class).to(UiApplicationDriver.class);
                break;
            case "odk":
                bind(OdkVersion.class).toInstance(OdkVersion.latest());
                bind(AndroidDevice.class).toInstance(AndroidDevice.latest());
                if(SauceLabsDriverProvider.isEnabled()) {
                    bind(AppiumDriver.class).toProvider(SauceLabsAppiumProvider.class).in(ScenarioScoped.class);
                } else {
                    bind(AppiumDriver.class).toProvider(LocalAppiumProvider.class).in(ScenarioScoped.class);
                }
                bind(ApplicationDriver.class).to(OdkApplicationDriver.class);
                break;
            default:
                throw new ConfigurationError("Invalid value for system property -Dapp.driver. " +
                        "Must be either 'web' or 'api'");
        }
        
        if(PostmarkStubServer.POSTMARK_STUB_PORT.isPresent()) {
            bind(EmailDriver.class).to(PostmarkStubClient.class);
            
        } else {
            bind(EmailDriver.class).to(MailinatorClient.class);
        }
    }
}
