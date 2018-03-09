/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.test.webdriver;

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
