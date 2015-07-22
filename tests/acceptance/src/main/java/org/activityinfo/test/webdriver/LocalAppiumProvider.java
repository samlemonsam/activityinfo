package org.activityinfo.test.webdriver;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import org.activityinfo.test.config.ConfigurationError;
import org.activityinfo.test.pageobject.odk.OdkApp;
import org.activityinfo.test.pageobject.odk.OdkVersion;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import javax.inject.Provider;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Provides a {@code AppiumDriver} instance that controls ODK Collect
 * running on an Android Device connected locally via USB.
 * 
 */
public class LocalAppiumProvider implements Provider<AppiumDriver> {
    
    private final OdkVersion odkVersion = OdkVersion.latest();

    @Override
    public AppiumDriver get() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(CapabilityType.BROWSER_NAME, "");
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "Android");
        capabilities.setCapability(MobileCapabilityType.APP, odkVersion.getApkLocalPath());
        capabilities.setCapability(MobileCapabilityType.APP_PACKAGE, OdkApp.ODK_COLLECT_PACKAGE);
        capabilities.setCapability(MobileCapabilityType.APP_ACTIVITY, OdkApp.ODK_MAIN_MENU);
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "LGOTMS3f380a07");
        capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "Selendroid");

        return new AndroidDriver(localUrl(), capabilities);
    }

    private URL localUrl() {
        try {
            return new URL("http://127.0.0.1:4723/wd/hub");
        } catch (MalformedURLException e) {
            throw new ConfigurationError("Invalid URL", e);
        }
    }
}
