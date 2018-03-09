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

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;


public class PhantomJsFactory implements PooledObjectFactory<PhantomJsInstance> {
    @Override
    public PooledObject<PhantomJsInstance> makeObject() throws Exception {
        PhantomJsInstance instance = new PhantomJsInstance();
        instance.start();
        
        return new DefaultPooledObject<>(instance);
    }

    @Override
    public boolean validateObject(PooledObject<PhantomJsInstance> p) {
        try {
            p.getObject().getWebDriver().getTitle();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void activateObject(PooledObject<PhantomJsInstance> p) throws Exception {
        p.getObject().getWebDriver().navigate().to("about:blank");
        p.getObject().getWebDriver().manage().deleteAllCookies();
        p.getObject().clearHomeDir();
    }

    @Override
    public void passivateObject(PooledObject<PhantomJsInstance> p) throws Exception {
        p.getObject().getWebDriver().navigate().to("about:blank");
    }

    @Override
    public void destroyObject(PooledObject<PhantomJsInstance> p) throws Exception {
        p.getObject().stop();
    }

}
