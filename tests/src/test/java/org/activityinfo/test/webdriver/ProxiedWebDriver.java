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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ProxiedWebDriver extends EventFiringWebDriver implements WebDriverConnection {

    private static final Logger LOGGER = Logger.getLogger(ProxiedWebDriver.class.getName());
    
    private ProxyController proxyController;
    

    public ProxiedWebDriver(WebDriver driver, ProxyController proxyController) {
        super(driver);
        this.proxyController = proxyController;
    }

    @Override
    public void quit() {
        try {
            proxyController.stop();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to shutdown proxy: " + e.getMessage(), e);
        }
        super.quit();
    }

    @Override
    public void setConnected(boolean connected) {
        proxyController.setConnected(connected);
    }
}
