package org.activityinfo.test.pageobject.web.design;

import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.gxt.GxtGrid;
import org.activityinfo.test.pageobject.gxt.GxtModal;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;
import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;


public class TargetsPage {


    private FluentElement container;

    public TargetsPage(FluentElement container) {
        this.container = container;
    }

    public GxtModal add() {
        container.find().button(withText("Add")).clickWhenReady();
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
        valueGrid().findCell(indicatorName, "value").edit(value);
    }
    
    public void setValue(String indicatorName, Double value) {
        valueGrid().findCell(indicatorName, "value").edit(Double.toString(value));
    }
}

