package org.activityinfo.test.webdriver;


import org.openqa.selenium.WebDriver;

public interface WebDriverSession {
    
    public WebDriver getDriver();
    
    public void finished(boolean passed);
}
