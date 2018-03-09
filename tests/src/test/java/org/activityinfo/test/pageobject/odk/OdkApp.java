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
package org.activityinfo.test.pageobject.odk;

import io.appium.java_client.AppiumDriver;

/**
 * Entry point for Page Objects representing the different
 * activities within the ODK Application.
 *
 */
public class OdkApp {
    public static final String ODK_COLLECT_PACKAGE = "org.odk.collect.android";
    public static final String ODK_MAIN_MENU = ".activities.MainMenuActivity";
    public static final String ODK_PREFERENCES = ".preferences.PreferencesActivity";

    private final AppiumDriver driver;


    public OdkApp(AppiumDriver driver) {
        this.driver = driver;
    }

    /**
     * Opens the ODK "General Settings" preferences
     * page
     *
     * @return the Preferences Page Object
     */
    public Preferences openGeneralSettings() {
        return new Preferences(driver).go();
    }

    /**
     * Opens the ODK FormList activity. Equivalent to
     * choose "Get Blank Forms" from the main menu
     *
     * @return the FormList Page Object
     */
    public FormList openFormList() {
        return new FormList(driver);
    }

    public MainMenu openMainMenu() {
        return new MainMenu(driver).go();
    }

    /**
     * Closes the ODK Collect app.
     */
    public void close() {
        try {
            driver.closeApp();
        } catch(Exception e) {
            // ignore, doesn't seem to work on SauceLabs
        }
    }
    
    public void quit() {
        driver.quit();
    }
}
