/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.test.pageobject.web.design;

import com.google.common.base.Optional;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.bootstrap.BsModal;
import org.activityinfo.test.pageobject.bootstrap.BsTable;
import org.activityinfo.test.pageobject.gxt.GalleryView;
import org.activityinfo.test.pageobject.gxt.GxtPanel;
import org.activityinfo.test.pageobject.gxt.GxtTree;
import org.activityinfo.test.pageobject.web.design.designer.FormModal;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;

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
                    databaseTree.select(I18N.CONSTANTS.databases(), databaseName);
                }
                return this;
            } catch (WebDriverException ignored) {}
        }
    }

    public LinkIndicatorsPage linkIndicators() {
        databaseTree.select("Link Indicators");
        return new LinkIndicatorsPage(container);
    }

    public DatabasesPage showDatabasesGrid() {
        databaseTree.select(I18N.CONSTANTS.databases());
        return new DatabasesPage(container);
    }
    
    private GalleryView gallery() {
        return GalleryView.find(container);
    }
    
    public TargetsPage targets() {
        gallery().select("Target");
        return new TargetsPage(container);
    }

    public UsersPage users() {
        gallery().select("Users");
        return new UsersPage(container);
    }


    public PartnerPage partners() {
        gallery().select("Partner");
        return new PartnerPage(container);
    }

    public DesignPage design() {
        gallery().select("Design");
        return new DesignPage(container);
    }

    public BsTable formInstanceTable() {
        return new BsTable(container.waitFor(By.className("cellTableWidget")));
    }

    public BsModal newDatabase() {
        container.find().button(withText("New Database")).waitForFirst().clickWhenReady();
        return modalDialog();
    }

    private BsModal modalDialog() {
        return BsModal.find(container);
    }

    public BsModal formInstance() {
        return FormModal.find(container);
    }

    public LocksPage locks() {
        gallery().select("Locks");
        return new LocksPage(container);
    }

    public FluentElement getContainer() {
        return container;
    }
}
