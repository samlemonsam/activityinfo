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
