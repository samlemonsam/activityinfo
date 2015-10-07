package org.activityinfo.test.ui;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.activityinfo.test.driver.UiApplicationDriver;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.gxt.GxtModal;
import org.activityinfo.test.pageobject.gxt.GxtTree;
import org.activityinfo.test.pageobject.web.design.DesignPage;
import org.activityinfo.test.pageobject.web.design.DesignTab;
import org.junit.Test;
import org.openqa.selenium.Keys;

import javax.inject.Inject;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static org.activityinfo.test.driver.Property.name;
import static org.activityinfo.test.driver.Property.property;
import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;

/**
 * @author yuriyz on 06/30/2015.
 */
public class DesignUiTest {

    private static final String DATABASE = "DesignUiDb";
    private static final String FORM = "Form";

    @Inject
    public UiApplicationDriver driver;

    @Test
    public void renameDatabase() throws Exception {
        driver.login();
        driver.setup().createDatabase(property("name", DATABASE));

        driver.renameDatabase(DATABASE, "NewDesignUiDb", "New Desc");

    }

    @Test // AI-877
    public void navigateAwayWithoutSavingChanges() throws Exception {
        driver.login();
        driver.setup().createDatabase(property("name", DATABASE));
        driver.setup().createForm(name(FORM),
                property("database", DATABASE),
                property("classicView", false));

        driver.ensureLoggedIn();

        String db = driver.getAliasTable().getAlias(DATABASE);
        String form = driver.getAliasTable().getAlias(FORM);

        DesignTab tab = driver.getApplicationPage().navigateToDesignTab().selectDatabase(db);
        DesignPage designPage = tab.design();

        GxtTree.GxtNode node = designPage.getDesignTree().select(form);
        FluentElement nodeElement = node.getElement();
        nodeElement.doubleClick();

        FluentElement editor = findInputEditor(tab.getContainer());
        editor.sendKeys("123");

        try {
            driver.getApplicationPage().navigateToDashboard();
        } catch (Exception e) {
            // ignore : our goal is to navigate away and make sure confirmation dialog appears
        }

        GxtModal confirmationModal = GxtModal.waitForModal(designPage.getContainer().root());
        assertNotNull(confirmationModal);
    }

    @Test // AI-878
    public void saveButtonState() throws Exception {
        driver.login();
        driver.setup().createDatabase(property("name", DATABASE));
        driver.setup().createForm(name(FORM),
                property("database", DATABASE),
                property("classicView", false));

        driver.ensureLoggedIn();

        String db = driver.getAliasTable().getAlias(DATABASE);
        String form = driver.getAliasTable().getAlias(FORM);

        DesignTab tab = driver.getApplicationPage().navigateToDesignTab().selectDatabase(db);
        DesignPage designPage = tab.design();

        GxtTree.GxtNode node = designPage.getDesignTree().select(form);
        FluentElement nodeElement = node.getElement();
        nodeElement.doubleClick();

        FluentElement editor = findInputEditor(tab.getContainer());
        editor.sendKeys("123", Keys.ENTER);

        designPage.getToolbarMenu().clickButton("Save");

        assertFalse(designPage.getToolbarMenu().button("Saved").isEnabled());

    }

    private FluentElement findInputEditor(FluentElement container) {
        return container.find().div(withClass("x-editor")).descendants().input(withClass("x-form-text")).first();
    }
}