/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.test.driver;

import com.google.inject.AbstractModule;
import cucumber.runtime.java.guice.ScenarioScoped;
import io.appium.java_client.AppiumDriver;
import org.activityinfo.test.config.ConfigurationError;
import org.activityinfo.test.driver.mail.EmailDriver;
import org.activityinfo.test.driver.mail.mailinator.MailinatorClient;
import org.activityinfo.test.driver.mail.postmark.PostmarkStubClient;
import org.activityinfo.test.driver.mail.postmark.PostmarkStubServer;
import org.activityinfo.test.pageobject.odk.OdkVersion;
import org.activityinfo.test.webdriver.AndroidDevice;
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
