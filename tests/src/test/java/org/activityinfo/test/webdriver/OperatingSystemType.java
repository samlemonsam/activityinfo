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

import java.util.Locale;

public enum OperatingSystemType {
    WINDOWS,
    OSX,
    LINUX,
    UNKNOWN;
    
    public static final OperatingSystemType host() {
        String osName = System.getProperty("os.name", "unknown").toLowerCase(Locale.ENGLISH);
        if ((osName.contains("mac")) || (osName.contains("darwin"))) {
            return OSX;
            
        } else if (osName.contains("windows")) {
            return WINDOWS;

        } else if (osName.contains("nux")) {
            return LINUX;
            
        } else {
            return UNKNOWN;
        }
    }
    
    public OperatingSystem version(String version) {
        return new OperatingSystem(this, version);
    }
    
    public OperatingSystem unknownVersion() {
        return new OperatingSystem(this, Version.UNKNOWN);
    }
}
