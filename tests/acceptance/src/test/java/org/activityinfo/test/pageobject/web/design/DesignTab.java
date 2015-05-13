package org.activityinfo.test.pageobject.web.design;

import com.google.common.base.Optional;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.bootstrap.BsModal;
import org.activityinfo.test.pageobject.gxt.GalleryView;
import org.activityinfo.test.pageobject.gxt.GxtPanel;
import org.activityinfo.test.pageobject.gxt.GxtTree;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;

public class DesignTab {

    private final FluentElement container;
    private GxtTree databaseTree;

    public DesignTab(FluentElement container) {
        this.container = container;
        this.databaseTree = GxtPanel.find(container, "Setup").tree();
    }
    
    public DesignTab selectDatabase(String databaseName) {

        while(true) {
            try {
                Optional<GxtTree.GxtNode> selected = databaseTree.findSelected();
                if (!selected.isPresent() || !selected.get().getLabel().equals(databaseName)) {
                    databaseTree.select("Databases", databaseName);
                }
                return this;
            } catch (StaleElementReferenceException ignored) {}
        }
    }
    
    private GalleryView gallery() {
        return GalleryView.find(container);
    }
    
    public TargetsPage targets() {
        gallery().select("Target");
        return new TargetsPage(container);
    }

    public PartnerPage partners() {
        gallery().select("Partner");
        return new PartnerPage(container);
    }

    public DesignPage design() {
        gallery().select("Design");
        return new DesignPage(container);
    }

    public FormInstanceTable formInstanceTable() {
        return new FormInstanceTable(container.waitFor(By.className("cellTableWidget")));
    }

    public BsModal newDatabase() {
        container.find().button(withText("New Database")).clickWhenReady();
        return modalDialog();
    }

    private BsModal modalDialog() {
        return new BsModal(container);
    }

    public BsModal formInstance() {
        container.waitFor(By.className("form-group")); // at least one form field appeared
        return modalDialog();
    }

}
