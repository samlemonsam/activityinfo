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
import com.mysql.jdbc.StringUtils;
import cucumber.api.DataTable;
import gherkin.formatter.model.DataTableRow;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.driver.AliasTable;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.api.XPathBuilder;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
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

    public static enum Type {
        CELL_TABLE("cellTableCell", "cellTableOddRow", "cellTableEvenRow"),
        GRID_TABLE("data-grid-cell", "data-grid-odd-row", "data-grid-even-row");

        private final String tdClass;
        private final String trOddClass;
        private final String trEvenClass;

        Type(String tdClass, String trOddClass, String trEvenClass) {
            this.tdClass = tdClass;
            this.trOddClass = trOddClass;
            this.trEvenClass = trEvenClass;
        }

        public String getTdClass() {
            return tdClass;
        }

        public String getTrOddClass() {
            return trOddClass;
        }

        public String getTrEvenClass() {
            return trEvenClass;
        }
    }

    public class Row {
        private final FluentElement container;

        public Row(FluentElement container) {
            this.container = container;
        }

        public FluentElement getContainer() {
            return container;
        }

        public List<Cell> getCells() {
            return Lists.newArrayList(container.find().td(withClass(type.getTdClass())).asList().as(Cell.class));
        }

    }

    public static class Cell {

        private final FluentElement container;

        public Cell(FluentElement container) {
            this.container = container;
        }

        public String text() {
            return container.text();
        }

        public FluentElement getContainer() {
            return container;
        }
    }

    private final FluentElement container;
    private final Type type;

    public BsTable(FluentElement container) {
        this(container, Type.CELL_TABLE);
    }

    public BsTable(FluentElement container, Type type) {
        this.container = container;
        this.type = type;
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

    public BsModal editSubmission() {
        button(I18N.CONSTANTS.edit()).get().clickWhenReady();
        container.root().waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return container.root().find().div(withClass("formPanel")).firstIfPresent().isPresent();
            }
        });
        return BsModal.find(container.root());
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

    public TableFilterDialog filter(String columnName) {
        try {
            clickOnHeader(columnName);
        } catch (NoSuchElementException e) {

            // handle built-in column names (without alias)
            int index = columnName.indexOf("_");
            if (index != -1) {
                clickOnHeader(columnName.substring(0, index));
            } else {
                throw e;
            }
        }
        return new TableFilterDialog(BsModal.find(container.root(), "modal-content"));
    }

    public void clickOnHeader(String columnName) {
        container.find().th().span(withText(columnName)).first().clickWhenReady();
    }

    public BsTable waitUntilAtLeastOneRowIsLoaded() {
        container.find().tagName("tr", false, withClass(type.getTrEvenClass()), withClass(type.getTrOddClass())).waitForFirst();
        return this;
    }

    public List<Row> rows() {
        // as() doesn't work because of inner class? convert manually
        List<FluentElement> elements = container.find().tagName("tr", false, withClass(type.getTrEvenClass()), withClass(type.getTrOddClass())).asList().list();
        List<Row> rows = Lists.newArrayList();
        for (FluentElement element : elements) {
            rows.add(new Row(element));
        }
        return rows;
//        return Lists.newArrayList(container.find().tagName("tr", false, withClass(type.getTrEvenClass()), withClass(type.getTrOddClass()))
//                .asList().as(Row.class));
    }

    public Optional<Cell> findCellByText(String cellText) {
        Optional<FluentElement> cell = container.find().td(withClass(type.getTdClass())).div(withText(cellText)).firstIfPresent();
        if (cell.isPresent()) {
            return Optional.of(new Cell(cell.get().find().ancestor().td(withClass(type.getTdClass())).first()));
        }
        return Optional.absent();
    }

    public Cell waitForCellByText(String cellText) {
        FluentElement cell = container.find().td(withClass(type.getTdClass())).div(withText(cellText)).waitForFirst();
        return new Cell(cell.find().ancestor().td(withClass(type.getTdClass())).first());
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

    public BsTable assertRowsPresent(DataTable dataTable) {
        List<DataTableRow> matched = Lists.newArrayList();
        List<DataTableRow> notMatched = Lists.newArrayList();

        List<Row> rows = rows();
        int expectedRows = dataTable.getGherkinRows().size() - 2;
        if (rows.size() != expectedRows) {
            throw new AssertionError("Number of rows do not match, found on UI: " + rows.size() + ", expected: " + expectedRows);
        }

        for (Row row : rows) {
            for (int i = 2; i < dataTable.getGherkinRows().size(); i++) {
                DataTableRow gherkinRow = dataTable.getGherkinRows().get(i);
                if (matched(row.getCells(), gherkinRow)) {
                    matched.add(gherkinRow);
                } else {
                    notMatched.add(gherkinRow);
                }
            }
        }
        if (matched.size() == expectedRows) {
            return this; // all are matched
        }

        throw new AssertionError("Unable to find match for field values: " + notMatched);
    }

    public static boolean matched(List<Cell> cells, DataTableRow row) {
        for (Cell cell : cells) {
            String text = cell.text().trim();
            String deAlias = AliasTable.deAlias(text);

            boolean cellMatched = false;
            for (int i = 0; i < row.getCells().size(); i++) {
                String cellStr = row.getCells().get(i);

                System.out.println("uiText: " + text + ", cellStr: " + cellStr);
                if (text.equals(cellStr) || deAlias.equals(cellStr) || text.isEmpty()) {
                    cellMatched = true;
                    break;
                }

                if (text.contains(",") && cellStr.contains(",")) { // check enum values
                    List<String> deAliasedEnumValues = AliasTable.deAliasEnumValueSplittedByComma(text);
                    if (!deAliasedEnumValues.isEmpty()) {
                        deAliasedEnumValues.removeAll(StringUtils.split(cellStr, ",", true));
                        if (deAliasedEnumValues.isEmpty()) { // all enum values are matched
                            cellMatched = true;
                            break;
                        }
                    }
                }
            }
            if (!cellMatched) {
                return false;
            }
        }
        return true;
    }

    public BsTable showAllColumns() {
        ChooseColumnsDialog dialog = chooseColumns();
        dialog.showAllColumns();
        dialog.ok();
        dialog.getModal().waitUntilClosed();
        return this;
    }

    public BsTable hideBuiltInColumns() {
        ChooseColumnsDialog dialog = chooseColumns();
        dialog.hideBuiltInColumns();
        dialog.ok();
        dialog.getModal().waitUntilClosed();
        return this;
    }

    private void buttonClick(String buttonName) {
        try {
            button(buttonName).get().clickWhenReady();
        } catch (TimeoutException e) {
            // it can not be : sauce lab shows that button is visible but for some reason it times out, so here we just retry
            button(buttonName).get().click();
        }
    }

    public ChooseColumnsDialog chooseColumns() {
        buttonClick(I18N.CONSTANTS.chooseColumns());

        container.root().waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return container.root().find().div(withClass(BsModal.CLASS_NAME)).firstIfPresent().isPresent();
            }
        });
        return new ChooseColumnsDialog(BsModal.find(container.root()));
    }

    public Type getType() {
        return type;
    }

    public BsTable waitUntilColumnShown(final String columnName) {
        container.waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return container.find().span(withText(columnName)).firstIfPresent().isPresent();
            }
        });
        return this;
    }
}
