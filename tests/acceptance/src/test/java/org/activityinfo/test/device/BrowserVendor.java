package org.activityinfo.test.device;

/**
* Created by alex on 26-1-15.
*/
public enum BrowserVendor {
    INTERNET_EXPLORER("internet explorer"),
    SAFARI("safari"),
    CHROME("firefox");

    private String sauceId;

    BrowserVendor(String sauceId) {
        this.sauceId = sauceId;
    }

    public String sauceId() {
        return sauceId;
    }
}
