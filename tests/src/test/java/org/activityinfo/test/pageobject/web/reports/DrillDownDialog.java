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
package org.activityinfo.test.pageobject.web.reports;

import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.gxt.GxtGrid;
import org.activityinfo.test.pageobject.gxt.GxtModal;
import org.openqa.selenium.By;

/**
 * @author yuriyz on 05/25/2015.
 */
public class DrillDownDialog {

    private FluentElement dialog;

    public DrillDownDialog(FluentElement root) {
        this.dialog = root.root().waitFor(By.className("x-window-body"));
    }

    public GxtGrid table() {
        return GxtGrid.findGrids(dialog).first().get();
    }

    public void close() {
        new GxtModal(dialog).close();
    }
}
