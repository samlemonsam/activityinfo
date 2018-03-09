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

import com.google.common.base.Predicate;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class FillBlankForms {

    private AppiumDriver driver;
    private WebDriverWait wait;

    public FillBlankForms(AppiumDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, 5);
    }

    public Question choose(String formName) {
        driver.findElementByPartialLinkText(formName).click();

        wait.until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                String source = driver.getPageSource();
                if(source.contains("forward")) {
                    System.out.println(source);
                }
                return source.contains("forward");
            }
        });

        Question startPage = new Question(driver);
        
        // includes instructions on how to move forward.
        // advance to the first question.
        
        return startPage.forward();
    }
}
