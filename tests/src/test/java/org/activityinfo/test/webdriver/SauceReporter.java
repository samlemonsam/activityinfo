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

import com.google.inject.Inject;
import com.saucelabs.common.Utils;
import com.saucelabs.saucerest.SauceREST;
import cucumber.api.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.util.HashMap;
import java.util.Map;

public class SauceReporter implements SessionReporter {


    private final WebDriverSession session;
    private final SauceLabsDriverProvider sauce;
    private Scenario scenario;

    @Inject
    public SauceReporter(WebDriverSession session, SauceLabsDriverProvider sauce) {
        this.session = session;
        this.sauce = sauce;
    }

    @Override
    public void start(Scenario scenario) {
        this.scenario = scenario;
    }

    @Override
    public void screenshot() {
        if(scenario != null) {
            try {
                TakesScreenshot driver = (TakesScreenshot) session.getDriver();
                byte[] screenshot = driver.getScreenshotAs(OutputType.BYTES);
                scenario.embed(screenshot, "image/png");
            } catch (Exception e) {
                scenario.write("Screenshot capture failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void finished(Scenario scenario) {


        String sessionId = session.getSessionId().toString();

        try {
            SauceREST sauceClient = sauce.getRestClient();
            Map<String, Object> updates = new HashMap<>();
            updates.put("passed", !scenario.isFailed());
            updates.put("name", scenario.getName());
            Utils.addBuildNumberToUpdate(updates);

            sauceClient.updateJobInfo(sessionId, updates);

            String jobName = System.getenv("JOB_NAME");
            if (jobName != null) {
                scenario.write(String.format("SauceOnDemandSessionID=%s job-name=%s\n", sessionId, jobName));
            }
        } catch(Exception e) {
            System.out.println("Failed to update sauce job status: " + e.getMessage());
        }

        // Add URL of Job to Cucumber output
        scenario.write(String.format("<a href=\"https://saucelabs.com/tests/%s\">Sauce Job Page</a>\n", sessionId));
    }
}
