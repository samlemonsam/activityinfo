package org.activityinfo.test.pageobject.gxt;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import cucumber.api.DataTable;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.DataTableRow;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.driver.TableData;
import org.activityinfo.test.pageobject.api.FluentElements;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.activityinfo.test.pageobject.api.XPathBuilder.containingText;
import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;
import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;


public class GxtGrid {
    
    private FluentElement container;

    

    public static FluentIterable<GxtGrid> findGrids(FluentElement container) {
        return container.findElements(By.className("x-grid3")).topToBottom().as(GxtGrid.class);
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

    private AssertionError makeAssertion(String text) {
        return new AssertionError(String.format("Could not find cell with text '%s'.", text) + extractData());
    }
    
    public DataTable extractData() {
        List<List<String>> rows = new ArrayList<>();
        
        List<String> headers = new ArrayList<>();
        for (FluentElement headerCell : container.findElements(By.xpath("//div[@role='columnheader']/span"))) {
            headers.add(headerCell.text().trim());
        }
        rows.add(headers);
        
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

    public void waitUntilAtLeastOneRowIsLoaded() {
        container.waitFor(By.className("x-grid3-row"));
    }


    public class GxtCell {
        private FluentElement element;

        public GxtCell(FluentElement element) {
            this.element = element;
        }
        
        public void edit(String value) {
            element.click();

            final FluentElement input = container.find().div(withClass("x-grid-editor")).input().waitForFirst();
            input.sendKeys(value);
            input.sendKeys(Keys.ENTER);
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
    }
    
}
