package org.activityinfo.test.pageobject.web.design;

import com.google.common.base.Predicate;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.driver.AliasTable;
import org.activityinfo.test.driver.FieldValue;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.gxt.GxtGrid;
import org.activityinfo.test.pageobject.gxt.GxtModal;
import org.activityinfo.test.pageobject.gxt.ToolbarMenu;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.List;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;
import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;


public class UsersPage {

    private FluentElement container;
    private ToolbarMenu toolbarMenu;

    public UsersPage(FluentElement container) {
        this.container = container;
        this.toolbarMenu = new ToolbarMenu(container.findElement(By.className("x-toolbar-ct")));
    }

    public ToolbarMenu getToolbarMenu() {
        return toolbarMenu;
    }

    public GxtGrid grid() {
        return GxtGrid.findGrids(container).get(0);
    }

    public GxtModal addUser() {
        container.waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                container.find().button(withText(I18N.CONSTANTS.addUser())).first().click();
                return container.root().exists(By.className(GxtModal.CLASS_NAME));
            }
        });
        return new GxtModal(container);
    }

    public UsersPage setPermissionForFirstRow(List<FieldValue> permissions) {
        setPermission(grid().rows().get(0),
                hasTrue("View", permissions),
                hasTrue("Edit", permissions),
                hasTrue("View All", permissions),
                hasTrue("Edit All", permissions),
                hasTrue("Manage users", permissions),
                hasTrue("Manage all users", permissions),
                hasTrue("Design", permissions)
        );
        return this;
    }

    private static boolean hasTrue(String name, List<FieldValue> permissions) {
        for (FieldValue fieldValue : permissions) {
            if (fieldValue.getField().equalsIgnoreCase(name) && AliasTable.isBoolean(fieldValue.getValue())) {
                return Boolean.parseBoolean(fieldValue.getValue());
            }
        }
        return false;
    }

    public UsersPage setPermission(GxtGrid.GxtRow row, boolean view, boolean viewAll, boolean edit, boolean editAll,
                                   boolean manageUsers, boolean manageAllUsers, boolean design) {
        if (!view) {
            row.getElement().find().div(withClass("x-grid3-cc-allowViewSimple")).first().clickWhenReady();
        }
        if (edit) {
            row.getElement().find().div(withClass("x-grid3-cc-allowEditSimple")).first().clickWhenReady();
        }
        if (viewAll) {
            row.getElement().find().div(withClass("x-grid3-cc-allowViewAll")).first().clickWhenReady();
        }
        if (editAll) {
            row.getElement().find().div(withClass("x-grid3-cc-allowEditAll")).first().clickWhenReady();
        }
        if (manageUsers) {
            row.getElement().find().div(withClass("x-grid3-cc-allowManageUsers")).first().clickWhenReady();
        }
        if (manageAllUsers) {
            row.getElement().find().div(withClass("x-grid3-cc-allowManageAllUsers")).first().clickWhenReady();
        }
        if (design) {
            row.getElement().find().div(withClass("x-grid3-cc-allowDesign")).first().clickWhenReady();
        }
        return this;
    }


}
