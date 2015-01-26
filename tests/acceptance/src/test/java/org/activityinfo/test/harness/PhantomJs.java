package org.activityinfo.test.harness;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.junit.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;


public class PhantomJs extends AbstractModule {

    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    public WebDriver createWebDriver() {
        PhantomJSDriver driver = new PhantomJSDriver();
        driver.manage().window().setSize(new Dimension(1400,1000));

        return driver;
    }
}
