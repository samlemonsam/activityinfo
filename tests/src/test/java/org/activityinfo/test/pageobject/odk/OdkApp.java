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
