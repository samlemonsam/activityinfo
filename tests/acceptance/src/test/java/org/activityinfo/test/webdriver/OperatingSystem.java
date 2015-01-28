package org.activityinfo.test.webdriver;

public enum OperatingSystem {
    WINDOWS_8_1("Windows 8.1"),
    WINDOWS_8("Windows 8"),
    WINDOWS_7("Windows 7"),
    WINDOWS_XP("Windows XP"),
    SNOW_LEOPARD("OS X 10.6"),
    MOUNTAIN_LION("OS X 10.6"),
    LINUX("Linux");

    private String sauceId;

    OperatingSystem(String sauceId) {
        this.sauceId = sauceId;
    }

    public String sauceId() {
        return sauceId;
    }
}
