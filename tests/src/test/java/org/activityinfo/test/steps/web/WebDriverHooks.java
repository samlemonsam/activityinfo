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
package org.activityinfo.test.steps.web;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.runtime.java.guice.ScenarioScoped;
import net.lightoze.gwt.i18n.server.ThreadLocalLocaleProvider;
import org.activityinfo.test.webdriver.*;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@ScenarioScoped
public class WebDriverHooks {


    @Inject 
    private WebDriverSession session;
    
    @Inject
    private SessionReporter reporter;
    
    private Scenario scenario;

    @Before
    public void before(Scenario scenario) {
        this.scenario = scenario;
        this.session.beforeTest(scenario.getName());
        reporter.start(scenario);

        ThreadLocalLocaleProvider.pushLocale(Locale.ENGLISH);
    }

    @After
    public void after(Scenario scenario) {
        if(session.isRunning()) {
            try {
                reporter.finished(scenario);
            } finally {
                session.stop();
            }
        }
        ThreadLocalLocaleProvider.popLocale();
    }

    @Given("^my browser supports offline mode$")
    public void my_browser_supports_offline_mode() throws Throwable {
    }

    @Given("^I am using (.*) on (.*)$")
    public void I_am_using_browser_version_on_os(String browser, String os) throws Throwable {
        session.setBrowserProfile(new BrowserProfile(parseOs(os), parseVendor(browser), extractVersion(browser)));
    }

    private String extractVersion(String browser) {
        Matcher matcher = Pattern.compile("[\\d\\\\.]+").matcher(browser);
        if(matcher.find()) {
            return matcher.group(0);
        } else {
            return Version.UNKNOWN.toString();
        }
    }

    private BrowserVendor parseVendor(String browser) {
        browser = browser.toLowerCase();
        if(browser.contains("ie") || browser.contains("internet")) {
            return BrowserVendor.IE;
        } else {
            for(BrowserVendor vendor : BrowserVendor.values()) {
                if(browser.contains(vendor.name().toLowerCase())) {
                    return vendor;
                }
            }
        }
        throw new IllegalArgumentException("Could not parse browser type. Should be one of " +
                Arrays.toString(BrowserVendor.values()));
    }

    private OperatingSystem parseOs(String os) {
        os = os.toLowerCase();
        if(os.contains("win")) {
            if(os.contains("xp")) {
                return new OperatingSystem(OperatingSystemType.WINDOWS, "XP");
            } else {
                return OperatingSystemType.WINDOWS.version(extractVersion(os));
            }
        } else if(os.contains("linux")) {
            return OperatingSystemType.LINUX.unknownVersion();
        } else if(os.contains("mac") || os.contains("os x") || os.contains("osx")) {
            return OperatingSystemType.OSX.version(extractVersion(os));
        }
        throw new IllegalArgumentException(os);
    }
}
