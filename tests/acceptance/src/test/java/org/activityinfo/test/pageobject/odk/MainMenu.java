package org.activityinfo.test.pageobject.odk;

import io.appium.java_client.AppiumDriver;

public class MainMenu {
    private AppiumDriver driver;

    public MainMenu(AppiumDriver driver) {
        this.driver = driver;
    }

    public MainMenu go() {
        driver.get("and-activity://org.odk.collect.android.activities.MainMenuActivity");

        return this;
    }

    public FillBlankForms fillBlankForms() {
        driver.findElementByPartialLinkText("Fill Blank Form").click();

        return new FillBlankForms(driver);
    }
}
