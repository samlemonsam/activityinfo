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
import org.activityinfo.test.pageobject.api.FluentElements;
import org.activityinfo.test.pageobject.gxt.Gxt;
import org.activityinfo.test.pageobject.web.reports.DashboardPortlet;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.List;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;
import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;


public class Dashboard {

    private final FluentElement container;

    public Dashboard(FluentElement container) {
        this.container = container;
    }
    
    public void assertAtLeastOnePortletIsVisible() {
        container.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.className(Gxt.PORTLET)));
    }
    
    public DashboardPortlet findPortlet(String name) {
        FluentElement portlet = container.find()
                .span(withClass("x-panel-header-text"), withText(name))
                .ancestor().div(withClass(Gxt.PORTLET))
                .waitForFirst();
        
        return new DashboardPortlet(portlet);
    }
    
    public List<String> getPortletTitles() {
        assertAtLeastOnePortletIsVisible();
        List<String> titles = new ArrayList<>();
        FluentElements headers = container.find().span(withClass("x-panel-header-text")).asList();
        for (FluentElement header : headers) {
            titles.add(header.text());
        }
        return titles;
    }
    
}
