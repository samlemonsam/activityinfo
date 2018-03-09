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
