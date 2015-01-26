package org.activityinfo.test.device;


public class DeviceProfile {

    private OperatingSystem os;
    private BrowserVendor browser;
    private String browserVersion;

    public DeviceProfile(OperatingSystem os, BrowserVendor browser, String browserVersion) {
        this.os = os;
        this.browser = browser;
        this.browserVersion = browserVersion;
    }

    public OperatingSystem getOS() {
        return os;
    }

    public BrowserVendor getBrowser() {
        return browser;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }
}
