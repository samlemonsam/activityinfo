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
