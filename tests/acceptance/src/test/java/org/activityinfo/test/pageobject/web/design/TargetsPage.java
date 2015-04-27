package org.activityinfo.test.pageobject.web.design;

import com.google.common.base.Predicate;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.gxt.GxtGrid;
import org.activityinfo.test.pageobject.gxt.GxtModal;
import org.activityinfo.test.pageobject.gxt.GxtTree;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

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
        targetGrid().findCell(targetName).click();
    }
    
    public void setValue(String indicatorName, String value) {
        expandTree(indicatorName); // expand tree first
        valueGrid().findCell(indicatorName, "value").edit(value);
    }

    public void setValue(String indicatorName, Double value) {
        expandTree(indicatorName); // expand tree first
        valueGrid().findCell(indicatorName, "value").edit(Double.toString(value));
    }

    private void expandTree(String indicatorName) {
        try {
            GxtTree tree = GxtTree.treeGrid(container);
            tree.waitUntilLoaded();
            tree.search(indicatorName).get().select();
        } catch (WebDriverException e) { // revisit it later
            // unknown error: cannot focus element on key down
        }
    }
}

