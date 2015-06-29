package org.activityinfo.test.pageobject.odk;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import java.util.List;

public class Preferences {

    private final AndroidDriver driver;

    public Preferences(AppiumDriver driver) {
        this.driver = (AndroidDriver)driver;
    }

    public Preferences go() {

        driver.get("and-activity://org.odk.collect.android.preferences.PreferencesActivity");

        //driver.startActivity(OdkApp.ODK_COLLECT_PACKAGE, ".preferences.PreferencesActivity");
        
        return this;
    }

    public Preferences setUrl(String url) {

        // Click the preference to open the dialog
        List<WebElement> urlButton = driver.findElements(By.partialLinkText("URL"));
        if(!urlButton.isEmpty()) {
            urlButton.get(0).click();
        } else {
            // Some versions say "Server" instead of URL
            driver.findElement(By.partialLinkText("Server")).click();
        }
        return updatePreference(url);
    }

    public Preferences setAccountEmail(String accountEmail) {

        // Click the preference to open the dialog
        choosePreference("Username");

        return updatePreference(accountEmail);
    }

    public Preferences setPassword(String password) {

        // Click the preference to open the dialog
        choosePreference("Password");

        // clear the existing dialog
        driver.findElement(By.xpath("//EditText")).clear();

        // update password
        driver.getKeyboard().sendKeys(password + "\n");
        
        driver.findElement(By.xpath("//Button[@value='OK']")).click();


        return this;
    }

    private void choosePreference(String name) {
        int retries = 5;
        while(true) {
            try {
                driver.findElement(By.partialLinkText(name)).click();
                return;
            } catch (StaleElementReferenceException e) {
                if (retries == 0) {
                    throw e;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException interrupted) {
                    throw new AssertionError("Interrupted.");
                }
                retries--;
            }
        }
    }

    private Preferences updatePreference(String newValue) {

        // clear the existing dialog
        WebElement entry = driver.findElement(By.xpath("//EditText"));
        entry.clear();

        // type in the new result
        driver.getKeyboard().sendKeys(newValue + "\n\n");


        //  driver.findElement(By.xpath("//Button[@value='OK']")).click();

        return this;
    }

}
