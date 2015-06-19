package org.activityinfo.test.pageobject.bootstrap;
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

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.api.XPathBuilder;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.Locatable;

import javax.annotation.Nullable;
import java.util.List;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;
import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;

/**
 * @author yuriyz on 06/09/2015.
 */
public class BsTable {

    public static class Row {
        private final FluentElement container;

        public Row(FluentElement container) {
            this.container = container;
        }

        public FluentElement getContainer() {
            return container;
        }
    }

    public static class Cell {
        private final FluentElement container;

        public Cell(FluentElement container) {
            this.container = container;
        }
    }

    private final FluentElement container;

    public BsTable(FluentElement container) {
        this.container = container;
    }

    private Optional<FluentElement> button(String buttonName) {
        return container.find().button(withClass("btn"), withText(buttonName)).firstIfPresent();
    }

    public static XPathBuilder tableXPath(FluentElement element) {
        return element.find().table(withClass("cellTableWidget"));
    }

    public static FluentIterable<BsTable> findTables(final FluentElement container) {
        container.waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return BsTable.tableXPath(container).firstIfPresent().isPresent();
            }
        });
        return BsTable.tableXPath(container).asList().as(BsTable.class);
    }

    public BsModal newSubmission() {
        button(I18N.CONSTANTS.newText()).get().clickWhenReady();
        container.root().waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return container.root().find().div(withClass("formPanel")).firstIfPresent().isPresent();
            }
        });
        return BsModal.find(container.root());
    }

    public List<Row> rows() {
        return Lists.newArrayList(container.find().tagName("tr", false, withClass("cellTableEvenRow"), withClass("cellTableOddRow"))
                .asList().as(Row.class));
    }

    public int rowCount() {
        return rows().size();
    }

    public FluentElement getContainer() {
        return container;
    }

    public void scrollToTheTop() {
        List<Row> rows = rows();
        WebElement lastRow = rows.get(0).getContainer().element();
        ((Locatable) lastRow).getCoordinates().inViewPort();
    }

    public void scrollToTheBottom() {
        List<Row> rows = rows();
        WebElement lastRow = rows.get(rows.size() - 1).getContainer().element();
        ((Locatable) lastRow).getCoordinates().inViewPort();
    }
}
