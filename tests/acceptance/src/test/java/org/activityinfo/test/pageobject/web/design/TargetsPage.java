package org.activityinfo.test.pageobject.web.design;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.driver.TestObject;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.gxt.GxtGrid;
import org.activityinfo.test.pageobject.gxt.GxtModal;
import org.activityinfo.test.pageobject.gxt.ToolbarMenu;
import org.activityinfo.test.pageobject.gxt.GxtTree;
import org.joda.time.LocalDate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;


public class TargetsPage {

    private ToolbarMenu toolbarMenu;
    private FluentElement container;

    public TargetsPage(FluentElement container) {
        this.container = container;
        this.toolbarMenu = new ToolbarMenu(container.findElement(By.className("x-toolbar-ct")));;
    }

    private GxtModal addOrEditButton(final String buttonName) {
        container.waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                container.find().button(withText(buttonName)).first().click();
                return container.root().exists(By.className(GxtModal.CLASS_NAME));
            }
        });
        return new GxtModal(container);
    }

    public GxtModal addButton() {
        return addOrEditButton(I18N.CONSTANTS.add());
    }

    public GxtModal editButton() {
        return addOrEditButton(I18N.CONSTANTS.edit());
    }

    public ToolbarMenu getToolbarMenu() {
        return toolbarMenu;
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
        expandTree(indicatorName); // expand tree first
        GxtGrid.GxtCell cell = valueGrid().findCell(indicatorName, "value");
        cell.edit(value);
    }
    
    public void setValue(String indicatorName, Double value) {
        expandTree(indicatorName); // expand tree first
        valueGrid().findCell(indicatorName, "value").edit(Double.toString(value));
    }

    /**
     * 
     * @return true if the user is currently visiting the targets page
     */
    public boolean isCurrentPage() {
        return container.getCurrentUri().getPath().contains("#targets/");
    }

    public void expandTree(String indicatorName) {
        GxtTree tree = GxtTree.treeGrid(container);
        try {
            tree.waitUntil(new Predicate<GxtTree>() {
                @Override
                public boolean apply(GxtTree tree) {
                    Optional<GxtTree.GxtNode> root = tree.firstRootNode();
                    boolean loaded = root.isPresent() && root.get().joint().firstIfPresent().isPresent();
                    return !loaded;
                }
            });
            tree.search(indicatorName).get().ensureExpanded();
        } catch (WebDriverException e) { // revisit it later
            // unknown error: cannot focus element on key down
        }
        Preconditions.checkState(tree.firstRootNode().get().joint().firstIfPresent().isPresent());
    }

    public TargetsPage createTarget(TestObject target) {
        return editTargetAndSave(addButton(), target.getAlias(), target.getAlias("partner"), target.getAlias("project"));
    }

    public TargetsPage editTarget(String targetName, String partner, String project) {
        select(targetName);
        return editTargetAndSave(editButton(), targetName, partner, project);
    }

    public TargetsPage editTarget(TestObject target) {
        return editTarget(target.getAlias(), target.getAlias("partner"), target.getAlias("project"));
    }

    private TargetsPage editTargetAndSave(GxtModal dialog, String targetName, String partner, String project) {
        dialog.form().fillTextField("Name", targetName);
        dialog.form().fillDateField("from", new LocalDate(2014, 1, 1));
        dialog.form().fillDateField("to", new LocalDate(2014, 12, 31));
        dialog.form().select("Partner", partner != null ? partner : I18N.CONSTANTS.none());
        dialog.form().select("Project", project != null ? project : I18N.CONSTANTS.none());
        dialog.accept();
        return this;
    }
}

