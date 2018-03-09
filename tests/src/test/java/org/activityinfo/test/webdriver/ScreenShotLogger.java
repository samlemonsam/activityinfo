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

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

@ScenarioScoped
public class ScreenShotLogger {

    public static int SCREENSHOT_SEQUENCE_NUMBER = 1;

    private final WebDriver driver;

    @Inject
    public ScreenShotLogger(WebDriver driver) {
        this.driver = driver;
    }

    public final void snapshot() {
        File targetDir = targetDir();
        File surefireReports = new File(targetDir, "surefire-reports");
        File attachmentDir = surefireReports; //new File(surefireReports, description.getClassName());
        attachmentDir.mkdirs();
        //testName = description.getMethodName();

        System.out.println("Current URL: " + driver.getCurrentUrl());

        try {
            if(driver instanceof TakesScreenshot) {
                File screenshotFile = new File(attachmentDir, "screenshot" + (SCREENSHOT_SEQUENCE_NUMBER++) + ".png");
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                Files.write(screenshot, screenshotFile);
                System.out.println("Screenshot saved to " + screenshotFile.getAbsolutePath());

            } else {
                File dumpFile = new File(attachmentDir, "screenshot" + (SCREENSHOT_SEQUENCE_NUMBER++) + ".html");

                Files.write(driver.getPageSource(), dumpFile, Charsets.UTF_8);
                System.out.println("Dumpfile saved to " + dumpFile.getCanonicalFile().getAbsolutePath());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private File targetDir() {
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath+"../../target");
        if(!targetDir.exists()) {
            targetDir.mkdir();
        }
        return targetDir;
    }



}
