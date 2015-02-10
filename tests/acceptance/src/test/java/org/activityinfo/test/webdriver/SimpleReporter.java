package org.activityinfo.test.webdriver;

import com.google.common.io.Files;
import cucumber.api.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

public class SimpleReporter implements SessionReporter {
    
    private WebDriverSession session;
    private int screenshotIndex = 1;

    @Inject
    public SimpleReporter(WebDriverSession session) {
        this.session = session;
    }

    @Override
    public void start(Scenario scenario) {
        
    }

    @Override
    public void finished(Scenario scenario) {
        if(scenario.isFailed()) {
            screenshot();
        }
    }

    @Override
    public void screenshot() {
        try {
            WebDriver driver = session.getDriver();
            if (driver instanceof TakesScreenshot) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                File screenshotFile = new File(targetDir(), "screenshot" + (screenshotIndex++) + ".png");
                Files.write(screenshot, screenshotFile);

                System.out.println("Wrote screenshot to " + screenshotFile.getCanonicalFile().toString());
            }
        } catch(Exception e) {
            System.out.println("Exception saving screenshot: " + e.getMessage());
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
