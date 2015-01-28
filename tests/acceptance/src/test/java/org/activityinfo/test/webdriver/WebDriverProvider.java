package org.activityinfo.test.webdriver;


import java.util.List;

public interface WebDriverProvider {
    
    List<? extends DeviceProfile> getSupportedProfiles();
    
    boolean supports(DeviceProfile profile);
    
    WebDriverSession start(DeviceProfile profile);
}
