package org.activityinfo.test.webdriver;


public interface WebDriverProvider {
    
    boolean supports(DeviceProfile profile);
    
    WebDriverSession start(DeviceProfile profile);
}
