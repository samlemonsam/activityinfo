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

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.openqa.selenium.support.events.EventFiringWebDriver;


public class PhantomJsPooledDriver extends EventFiringWebDriver implements WebDriverConnection {

    private GenericObjectPool<PhantomJsInstance> pool;
    private PhantomJsInstance instance;

    public PhantomJsPooledDriver(GenericObjectPool<PhantomJsInstance> pool, PhantomJsInstance instance) {
        super(instance.getWebDriver());
        this.pool = pool;
        this.instance = instance;
    }

    @Override
    public void quit() {
        pool.returnObject(instance);
    }

    @Override
    public void setConnected(boolean connected) {
        
    }
}
