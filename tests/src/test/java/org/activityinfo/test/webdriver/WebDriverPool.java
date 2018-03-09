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

import com.google.common.base.Function;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import java.util.concurrent.TimeUnit;

/**
 * Maintains a pool of WebDriver instances that can be reused
 */
public class WebDriverPool {
    
    private GenericKeyedObjectPool<BrowserProfile, WebDriver> pool;
    private Function<BrowserProfile, WebDriver> creator;

    public WebDriverPool() {
        pool = new GenericKeyedObjectPool<>(new Factory());
        pool.setTimeBetweenEvictionRunsMillis(TimeUnit.SECONDS.toMillis(5));
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                pool.close();
            }
        });
    }
    
    public void setMaxTotalSize(int size) {
        pool.setMaxTotal(size);
        pool.setMaxIdlePerKey(size);
    }

    public void setCreator(Function<BrowserProfile, WebDriver> creator) {
        this.creator = creator;
    }

    private WebDriver createDriver(BrowserProfile profile) {
        WebDriver driver = creator.apply(profile);
        configureDriver(driver);
        return driver;
    }

    private static void configureDriver(WebDriver driver) {
        // increase timeout before NoSuchElementException
        driver.manage().timeouts().implicitlyWait(4, TimeUnit.SECONDS);
    }

    public WebDriver get(BrowserProfile profile) {
        try {
            return new Wrapper(profile, pool.borrowObject(profile));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public class Factory implements KeyedPooledObjectFactory<BrowserProfile, WebDriver> {
    
        @Override
        public PooledObject<WebDriver> makeObject(BrowserProfile browserProfile) throws Exception {
            return new DefaultPooledObject<>(createDriver(browserProfile));
        }
        
        @Override
        public void destroyObject(BrowserProfile browserProfile, PooledObject<WebDriver> pooledObject) throws Exception {
            pooledObject.getObject().quit();    
        }
    
        @Override
        public boolean validateObject(BrowserProfile browserProfile, PooledObject<WebDriver> pooledObject) {
            try {
                pooledObject.getObject().getTitle();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    
        @Override
        public void activateObject(BrowserProfile browserProfile, PooledObject<WebDriver> pooledObject) throws Exception {
            pooledObject.getObject().manage().deleteAllCookies();
            pooledObject.getObject().navigate().to("about:blank");
        }
    
        @Override
        public void passivateObject(BrowserProfile browserProfile, PooledObject<WebDriver> pooledObject) throws Exception {
            pooledObject.getObject().navigate().to("about:blank");
            try {
                Alert alert = pooledObject.getObject().switchTo().alert();
                alert.accept();
            } catch (NoAlertPresentException ignored) {
                // continue
            }
        }
    }

    public class Wrapper extends EventFiringWebDriver implements WebDriverConnection {
    
        private BrowserProfile profile;
    
        public Wrapper(BrowserProfile profile, WebDriver driver) {
            super(driver);
            this.profile = profile;
        }
    
        @Override
        public void quit() {
            pool.returnObject(profile, getWrappedDriver());
        }

        @Override
        public void setConnected(boolean connected) {
            if(getWrappedDriver() instanceof WebDriverConnection) {
                ((WebDriverConnection) getWrappedDriver()).setConnected(connected);
            }
        }
    }
}
