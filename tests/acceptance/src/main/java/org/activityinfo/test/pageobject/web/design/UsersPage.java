package org.activityinfo.test.pageobject.web.design;

import com.google.common.base.Predicate;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.Sleep;
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

    public UsersPage setPermission(String email, List<FieldValue> permissions) {
        setPermission(email,
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

    public UsersPage setPermission(String email, boolean view, boolean viewAll, boolean edit, boolean editAll,
                                   boolean manageUsers, boolean manageAllUsers, boolean design) {

        if (!view) {
            rowByCellValue(email).getElement().find().div(withClass("x-grid3-cc-allowViewSimple")).first().clickWhenReady();
        }
        if (edit) {
            rowByCellValue(email).getElement().find().div(withClass("x-grid3-cc-allowEditSimple")).first().clickWhenReady();
        }
        if (viewAll) {
            rowByCellValue(email).getElement().find().div(withClass("x-grid3-cc-allowViewAll")).first().clickWhenReady();
        }
        if (editAll) {
            rowByCellValue(email).getElement().find().div(withClass("x-grid3-cc-allowEditAll")).first().clickWhenReady();
        }
        if (manageUsers) {
            rowByCellValue(email).getElement().find().div(withClass("x-grid3-cc-allowManageUsers")).first().clickWhenReady();
        }
        if (manageAllUsers) {
            rowByCellValue(email).getElement().find().div(withClass("x-grid3-cc-allowManageAllUsers")).first().clickWhenReady();
        }
        if (design) {
            rowByCellValue(email).getElement().find().div(withClass("x-grid3-cc-allowDesign")).first().clickWhenReady();
        }
        toolbarMenu.clickButton(I18N.CONSTANTS.save());
        Sleep.sleepSeconds(1);
        return this;
    }

    private GxtGrid.GxtRow rowByCellValue(String value) {
        return new GxtGrid.GxtRow(grid().findCell(value).getElement().find().ancestor().div(withClass(GxtGrid.X_GRID3_ROW)).first());
    }


}
