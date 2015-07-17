package org.activityinfo.test.odk;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

public class AndroidDevice {

    public static final String GENERIC_EMULATOR = "Android Emulator";
    public static final String SAMSUNG_GALAXY_TAB = "Samsung Galaxy Tab 3 Emulator";
    public static final String HTC_ONE_X_EMULATOR = "HTC ONE X Emulator";
    public static final String SAMSUNG_GALAXY_NOTE = "Samsung Galaxy Note Emulator";


    private String deviceName;
    private String platformVersion;

    public AndroidDevice(String platformVersion) {
        this.deviceName = GENERIC_EMULATOR;
        this.platformVersion = platformVersion;
    }

    public AndroidDevice(String deviceName, String platformVersion) {
        this.deviceName = deviceName;
        this.platformVersion = platformVersion;
    }

    public static AndroidDevice latest() {
        return new AndroidDevice("4.2");
    }
    
    public String getDeviceName() {
        return deviceName;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    @Override
    public String toString() {
        return deviceName + " " + platformVersion;
    }

    /**
     *
     * @return the list of emulator profiles supported by Sauce (and that we're interested in testing)
     * @see <a href="https://saucelabs.com/platforms/appium">Complete List</a>
     */
    public static List<AndroidDevice> selected() {

        if(Strings.isNullOrEmpty("device")) {

            List<AndroidDevice> devices = Lists.newArrayList();

            devices.add(new AndroidDevice("4.3"));

            devices.add(new AndroidDevice("4.2"));
            devices.add(new AndroidDevice(SAMSUNG_GALAXY_TAB, "4.2"));

            devices.add(new AndroidDevice("4.1"));
            devices.add(new AndroidDevice(HTC_ONE_X_EMULATOR, "4.1"));

            devices.add(new AndroidDevice("4.0"));
            devices.add(new AndroidDevice(SAMSUNG_GALAXY_NOTE, "4.0"));

            devices.add(new AndroidDevice("2.3"));
            devices.add(new AndroidDevice(SAMSUNG_GALAXY_NOTE, "2.3"));

            return devices;

        } else {
            return Collections.singletonList(new AndroidDevice("4.3"));
        }
    }


}
