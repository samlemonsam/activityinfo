package org.activityinfo.test.webdriver;


import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;

public class BrowserProfile implements DeviceProfile, Serializable {

    @Nonnull
    private final OperatingSystem os;

    @Nonnull
    private final BrowserVendor browser;

    @Nonnull
    private final String browserVersion;

    private final Set<String> tags;
    
    public BrowserProfile(OperatingSystem os, BrowserVendor browser, String browserVersion) {
        this.os = os;
        this.browser = browser;
        this.browserVersion = browserVersion;
        this.tags = ImmutableSet.of(browser.tag());
    }

    public OperatingSystem getOS() {
        return os;
    }

    public BrowserVendor getVendor() {
        return browser;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    @Override
    public Serializable getId() {
        return this;
    }

    @Override
    public String getName() {
        return Joiner.on(" ").join(Arrays.asList(os.name(), browser, browserVersion));
    }

    @Override
    public Set<String> getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BrowserProfile that = (BrowserProfile) o;

        if (browser != that.browser) return false;
        if (!browserVersion.equals(that.browserVersion)) return false;
        if (os != that.os) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = os.hashCode();
        result = 31 * result + browser.hashCode();
        result = 31 * result + browserVersion.hashCode();
        return result;
    }
}
