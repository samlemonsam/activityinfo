package org.activityinfo.test.pageobject.api;

import org.openqa.selenium.By;

public class XPath {
    
    public static By query(String query, Object... args) {
        return By.xpath(String.format(query, args));
    }
}
