package org.activityinfo.test.pageobject.web.design;

import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.gxt.GalleryView;
import org.activityinfo.test.pageobject.gxt.GxtPanel;
import org.activityinfo.test.pageobject.gxt.GxtTree;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class DesignTab {

    private final FluentElement container;
    private GxtTree databaseTree;
    
    public DesignTab(FluentElement container) {
        this.container = container;
        this.databaseTree = GxtPanel.find(container, "Setup").tree();
    }
    
    public DesignTab selectDatabase(String databaseName) {
        databaseTree.select("Databases", databaseName);
        return this;
    }
    
    private GalleryView gallery() {
        return GalleryView.find(container);
    }
    
    public TargetsPage targets() {
        gallery().select("Target");
        return new TargetsPage(container);
    }
}
