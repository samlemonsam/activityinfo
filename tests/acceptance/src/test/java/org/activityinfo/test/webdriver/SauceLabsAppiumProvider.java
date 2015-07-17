package org.activityinfo.test.webdriver;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import cucumber.runtime.java.guice.ScenarioScoped;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.activityinfo.test.odk.AndroidDevice;
import org.activityinfo.test.pageobject.odk.OdkApp;
import org.activityinfo.test.pageobject.odk.OdkVersion;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import javax.inject.Provider;
import java.util.Set;
import java.util.logging.Logger;

@ScenarioScoped
public class SauceLabsAppiumProvider implements Provider<AppiumDriver> {

    private static final Logger LOGGER = Logger.getLogger(SauceLabsAppiumProvider.class.getName());
    
    public static final int ALREADY_UPLOADED = 400;
    public static final int SUCCESS = 200;
    
    private final SauceLabsDriverProvider sauceLabs;
    
    private final Set<OdkVersion> uploadedVersions = Sets.newHashSet();

    private final AndroidDevice device;
    private final OdkVersion odkVersion;

    @Inject
    public SauceLabsAppiumProvider(SauceLabsDriverProvider sauceLabs, AndroidDevice device, OdkVersion odkVersion) {
        this.sauceLabs = sauceLabs;
        this.device = device;
        this.odkVersion = odkVersion;
    }


    @Override
    public AppiumDriver get() {

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(CapabilityType.BROWSER_NAME, "");
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("deviceName", device.getDeviceName());
        capabilities.setCapability("platformVersion", device.getPlatformVersion());
        capabilities.setCapability("app", getApkUrl(odkVersion));
        capabilities.setCapability("appPackage", OdkApp.ODK_COLLECT_PACKAGE);
        capabilities.setCapability("appActivity", OdkApp.ODK_PREFERENCES);
        capabilities.setCapability("automationName", "Selendroid");
        
        return new AndroidDriver(sauceLabs.getWebDriverServer(), capabilities);
    }

    public String getApkUrl(OdkVersion version)  {
        // We have our own collection of ODK APKs in the BeDataDriven Development
        // Project
        String url = "http://storage.googleapis.com/odk-apk/" + version.getApkName();
        
        LOGGER.info("APK URL = " + url);
        return url;
    }

}
