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

import com.google.common.base.Predicate;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.api.XPathBuilder;
import org.activityinfo.test.pageobject.bootstrap.BsTable;
import org.openqa.selenium.WebDriver;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;

/**
 * @author yuriyz on 06/09/2015.
 */
public class TablePage {

    private final FluentElement page;

    public TablePage(FluentElement page) {
        this.page = page;
        page.waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return tableXPath().firstIfPresent().isPresent();
            }
        });
    }

    private XPathBuilder tableXPath() {
        return page.find().table(withClass("cellTableWidget"));
    }

    public BsTable table() {
        return new BsTable(tableXPath().first());
    }
}
