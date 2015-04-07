package org.activityinfo.test.pageobject.web.design;

import com.google.common.base.Optional;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.gxt.GalleryView;
import org.activityinfo.test.pageobject.gxt.GxtPanel;
import org.activityinfo.test.pageobject.gxt.GxtTree;

public class DesignTab {

    private final FluentElement container;
    private GxtTree databaseTree;

    public DesignTab(FluentElement container) {
        this.container = container;
        this.databaseTree = GxtPanel.find(container, "Setup").tree();
    }
    
    public DesignTab selectDatabase(String databaseName) {

        Optional<GxtTree.GxtNode> selected = databaseTree.findSelected();
        if(!selected.isPresent() || !selected.get().getLabel().equals(databaseName)) {
            databaseTree.select("Databases", databaseName);
        }
        return this;
    }
    
    private GalleryView gallery() {
        return GalleryView.find(container);
    }
    
    public TargetsPage targets() {
        gallery().select("Target");
        return new TargetsPage(container);
    }

    public DesignPage design() {
        gallery().select("Design");
        return new DesignPage(container);
    }
}
