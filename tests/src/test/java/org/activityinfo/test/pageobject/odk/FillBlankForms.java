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
