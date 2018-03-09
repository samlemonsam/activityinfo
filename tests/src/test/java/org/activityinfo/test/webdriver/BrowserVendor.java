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

/**
* Created by alex on 26-1-15.
*/
public enum BrowserVendor {
    IE("internet explorer", "ie"),
    SAFARI("safari"),
    CHROME("chrome"),
    FIREFOX("firefox"),
    OPERA("opera");

    private String sauceId;
    private String tag;

    BrowserVendor(String sauceId) {
        this(sauceId, sauceId);
    }

    BrowserVendor(String sauceId, String tag) {
        this.sauceId = sauceId;
        this.tag = tag;
    }

    public String sauceId() {
        return sauceId;
    }
    
    public String tag() {
        return tag;
    }
}
