package org.activityinfo.test.pageobject.gxt;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import cucumber.api.DataTable;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.api.FluentElements;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.activityinfo.test.pageobject.api.XPathBuilder.*;


public class GxtGrid {
    
    private static final Logger LOGGER = Logger.getLogger(GxtGrid.class.getName());
    
    private FluentElement container;

    

    public static FluentIterable<GxtGrid> findGrids(final FluentElement container) {
        FluentElements elements = container.waitFor(new Function<WebDriver, FluentElements>() {
            @Nullable
            @Override
            public FluentElements apply(WebDriver input) {
                FluentElements grids = container.findElements(By.className("x-grid3"));
                if(grids.isEmpty()) {
                    return null;
                } 
                return grids;
            }
        });
        
        return elements.topToBottom().as(GxtGrid.class);
    }
    
    public GxtGrid(FluentElement container) {
        this.container = container;
    }
    
    public GxtCell findCell(String text) {
        Optional<FluentElement> cell = container.find()
                .div(withClass("x-grid3-cell-inner"), containingText(text))
                .ancestor().td(withClass("x-grid3-cell"))
                .firstIfPresent();
        
        if(!cell.isPresent()) {
            throw makeAssertion(text);
        }
        
        return new GxtCell(cell.get());
    }
    
    public void sortBy(String columnId) {
        columnHeader(columnId).click();
        try {
            waitUntilReloaded();
        } catch (InterruptedException e) {
            throw new AssertionError("Interrupted while waiting for grid to reload");
        }
    }
    
    public FluentElement columnHeader(String columnId) {
        return container.find().div(withClass("x-grid3-hd-" + columnId)).first();
    }
    
    public List<String> columnValues(String columnId) {
        FluentElements cells = container.find()
                .div(withClass("x-grid3-cell-inner"), withClass("x-grid3-col-" + columnId))
                .asList();
        
        List<String> values = new ArrayList<>();
        for (FluentElement cell : cells) {
            values.add(cell.text().trim());
        }
        return values;
    }

    private AssertionError makeAssertion(String text) {
        String dataTable;
        try {
            dataTable = extractData().toString();
        } catch (Exception e) {
            dataTable = "<Error: >";
        }
        return new AssertionError(String.format("Could not find cell with text '%s'.", text) + dataTable);
    }
    
    public FluentIterable<GxtRow> rows() {
        return container.findElements(By.className("x-grid3-row")).as(GxtRow.class);
    }

    public DataTable extractData() {
        return extractData(true);
    }
    
    public DataTable extractData(boolean withHeader) {
        List<List<String>> rows = new ArrayList<>();

        if (withHeader) {
            List<String> headers = new ArrayList<>();
            for (FluentElement headerCell : container.findElements(By.xpath("//div[@role='columnheader']/span"))) {
                headers.add(headerCell.text().trim());
            }
            rows.add(headers);
        }

        for (FluentElement row : container.findElements(By.className("x-grid3-row"))) {
            List<String> cells = Lists.newArrayList();
            for (FluentElement cell : row.findElements(By.className("x-grid3-cell"))) {
                cells.add(cell.text());
            }
            rows.add(cells);
        }
        return DataTable.create(rows);
    }

    public GxtCell findCell(String rowText, String columnId) {
        return new GxtCell(container.find()
                .anyElement(withText(rowText))
                .ancestor().div(withClass("x-grid3-row"))
                .descendants().td(withClass("x-grid3-td-" + columnId))
                .first());
    }
    
    public GxtGrid waitUntilReloaded() throws InterruptedException {
        
        // Wait until the loading mask appears
        FluentElement loadingMask = container.root().waitFor(By.className("ext-el-mask"));

        // Wait until it disappears...
        loadingMask.waitUntil(ExpectedConditions.stalenessOf(loadingMask.element()));
        return this;
    }

    public GxtGrid waitUntilAtLeastOneRowIsLoaded() {
        container.waitFor(By.className("x-grid3-row"));
        return this;
    }


    public static class GxtRow {
        private FluentElement element;

        public GxtRow(FluentElement element) {
            this.element = element;
        }
        
        public void select() {
            element.click();
        }
    }
    
    public class GxtCell {
        private FluentElement element;

        public GxtCell(FluentElement element) {
            this.element = element;
        }
        
        public void edit(String value) {
            
            // If there is a currently focused cell, clear it first.
            Optional<FluentElement> focusedElement = element.focusedElement();
            if(focusedElement.isPresent()) {
                focusedElement.get().sendKeys(Keys.ESCAPE);
            }

            element.click();

            final FluentElement input = container.find()
                    .div(withClass("x-grid-editor"))
                    .input(withClass("x-form-focus"))
                    .waitForFirst();
            
            input.clear();
            if(!"<blank>".equals(value)) {
                input.sendKeys(value);
            }
            input.sendKeys(Keys.TAB);
            container.waitUntil(new Predicate<WebDriver>() {
                @Override
                public boolean apply(WebDriver driver) {
                    try {
                        return !input.isDisplayed();
                    } catch (StaleElementReferenceException ignored) {
                        return true;
                    }
                }
            });
        }

        public void click() {
            element.click();
        }

        public void doubleClick() {
            element.doubleClick();
        }
    }
    
}
