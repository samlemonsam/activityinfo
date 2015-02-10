package org.activityinfo.test.pageobject.gxt;

import org.activityinfo.test.pageobject.api.FluentElement;
import org.openqa.selenium.By;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;
import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;

public class GxtPanel {

    private final FluentElement panel;

    public static GxtPanel find(FluentElement container, String heading) {
        FluentElement panel = container.find().span(withText(heading)).ancestor().div(withClass("x-panel")).waitForFirst();
        return new GxtPanel(panel);
    }


    public GxtPanel(FluentElement panel) {
        this.panel = panel;
    }
    
    public GxtTree tree() {
        return new GxtTree(panel.findElement(By.className("x-tree3")));
    }
}
