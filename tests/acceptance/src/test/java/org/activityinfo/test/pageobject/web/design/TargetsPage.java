package org.activityinfo.test.pageobject.web.design;

import com.google.common.base.Predicate;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.gxt.GxtGrid;
import org.activityinfo.test.pageobject.gxt.GxtModal;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;


public class TargetsPage {


    private FluentElement container;

    public TargetsPage(FluentElement container) {
        this.container = container;
    }

    public GxtModal add() {
        container.waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                container.find().button(withText("Add")).first().click();
                return container.root().exists(By.className(GxtModal.CLASS_NAME));
            }
        });
        return new GxtModal(container);
    }

    public GxtGrid targetGrid() {
        return GxtGrid.findGrids(container).get(0);
    }
    
    public GxtGrid valueGrid() {
        return GxtGrid.findGrids(container).get(1);
    }
    
    public void select(String targetName) {
        // Select the target from the upper grid
        GxtGrid grid = targetGrid();
        grid.waitUntilAtLeastOneRowIsLoaded();
        grid.findCell(targetName).click();
    }
    
    public void setValue(String indicatorName, String value) {
        GxtGrid.GxtCell cell = valueGrid().findCell(indicatorName, "value");
        cell.edit(value);
    }
    
    public void setValue(String indicatorName, Double value) {
        valueGrid().findCell(indicatorName, "value").edit(Double.toString(value));
    }

    /**
     * 
     * @return true if the user is currently visiting the targets page
     */
    public boolean isCurrentPage() {
        return container.getCurrentUri().getPath().contains("#targets/");
    }
}

