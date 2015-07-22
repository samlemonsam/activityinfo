package org.activityinfo.test.pageobject.web.design;
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

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.gxt.GxtGrid;
import org.activityinfo.test.pageobject.gxt.GxtModal;
import org.activityinfo.test.pageobject.gxt.ToolbarMenu;
import org.activityinfo.test.pageobject.web.components.Form;

/**
 * @author yuriyz on 06/30/2015.
 */
public class DatabasesPage {

    private final FluentElement container;

    public DatabasesPage(FluentElement container) {
        this.container = container;
    }

    public GxtGrid grid() {
        return GxtGrid.findGrids(container).first().get();
    }

    public void buttonClick(String buttonName) {
        ToolbarMenu.find(container).clickButton(buttonName);
    }

    public DatabasesPage rename(String oldName, String newName, String newDescription) {
        grid().findCell(oldName).click();
        buttonClick(I18N.CONSTANTS.renameDatabase());
        GxtModal modal = new GxtModal(container);

        Form form = modal.form();
        form.findFieldByLabel(I18N.CONSTANTS.name()).fill(newName);
        form.findFieldByLabel(I18N.CONSTANTS.description()).fill(newDescription);
        modal.accept();
        return this;
    }

}
