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

import com.google.inject.Singleton;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.openqa.selenium.WebDriver;

@Singleton
public class PhantomJsProvider implements WebDriverProvider {

    public static final BrowserProfile BROWSER_PROFILE = new BrowserProfile(OperatingSystem.host(),
            BrowserVendor.CHROME, "phantom.js");

    private final GenericObjectPool<PhantomJsInstance> pool;

    public PhantomJsProvider() {
        pool = new GenericObjectPool<>(new PhantomJsFactory());
        pool.setMaxTotal(3);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                pool.close();
            }
        });
    }

    @Override
    public WebDriver start(String name, BrowserProfile profile) {
        try {
            return new PhantomJsPooledDriver(pool, pool.borrowObject());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
