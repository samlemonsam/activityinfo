package org.activityinfo.test.webdriver;


import org.openqa.selenium.WebDriver;

public interface WebDriverProvider {

    WebDriver start(String name, BrowserProfile profile);

}
