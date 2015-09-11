package org.activityinfo.test.pageobject.web.entry;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.collect.Lists;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.api.FluentElements;
import org.activityinfo.test.pageobject.api.XPathBuilder;
import org.activityinfo.test.pageobject.gxt.Gxt;
import org.openqa.selenium.By;

import java.util.List;

/**
 * @author yuriyz on 08/17/2015.
 */
public class DataEntryFilter {

    private FluentElement container;
    private String label;

    public DataEntryFilter(FluentElement container, String label) {
        this.container = container;
        this.label = label;
    }

    public DataEntryFilter select() {
        return select(true);
    }

    public DataEntryFilter select(boolean wait) {
        container.clickWhenReady();
        if (wait) {
            Gxt.sleepSeconds(2);
        }
        return this;
    }

    private FluentElement body() {
        return container.find().div(XPathBuilder.withClass("x-panel-body")).first();
    }

    private FluentElements items() {
        return body().findElements(By.className("x-view-item"));
    }

    public List<String> filterItems() {
        List<String> items = Lists.newArrayList();
        for (FluentElement element : items().list()) {
            items.add(element.text());
        }
        return items;
    }
}
