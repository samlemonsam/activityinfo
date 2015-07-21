package org.activityinfo.test.webdriver;

import com.google.inject.AbstractModule;
import cucumber.runtime.java.guice.ScenarioScoped;
import io.appium.java_client.AppiumDriver;
import org.activityinfo.test.driver.ApplicationDriver;
import org.activityinfo.test.driver.OdkApplicationDriver;
import org.activityinfo.test.pageobject.odk.OdkVersion;


public class OdkModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(OdkVersion.class).toInstance(OdkVersion.latest());
        bind(AndroidDevice.class).toInstance(AndroidDevice.latest());
        if(SauceLabsDriverProvider.isEnabled()) {
            bind(AppiumDriver.class).toProvider(SauceLabsAppiumProvider.class).in(ScenarioScoped.class);
        } else {
            bind(AppiumDriver.class).toProvider(LocalAppiumProvider.class).in(ScenarioScoped.class);
        }
        bind(ApplicationDriver.class).to(OdkApplicationDriver.class);
    }
}
