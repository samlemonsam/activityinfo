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
package org.activityinfo.test.pageobject.web;

import org.activityinfo.test.pageobject.api.FluentElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;

import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;

/**
 * Interface to the single-pageobject application
 */
public class ApplicationPage {

    private final FluentElement page;

    @Inject
    public ApplicationPage(WebDriver webDriver) {
        this.page = new FluentElement(webDriver);        
    }
    
    public ApplicationPage(FluentElement page) {
        this.page = page;
    }

    public void waitUntilLoaded() {
        page.waitUntil(invisibilityOfElementLocated(By.id("loading")));
    }


}
