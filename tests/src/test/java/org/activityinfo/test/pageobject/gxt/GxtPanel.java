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
package org.activityinfo.test.pageobject.gxt;

import org.activityinfo.test.pageobject.api.FluentElement;
import org.openqa.selenium.By;

import static org.activityinfo.test.pageobject.api.XPathBuilder.*;

public class GxtPanel {

    private final FluentElement panel;

    public static GxtPanel find(FluentElement container, String heading) {
        FluentElement panel = container.find().span(withText(heading)).ancestor().div(withClass("x-panel")).waitForFirst();
        return new GxtPanel(panel);
    }

    public static GxtPanel findStartsWith(FluentElement container, String headingStartsWith) {
        FluentElement panel = container.find().span(containingText(headingStartsWith)).ancestor().div(withClass("x-panel")).waitForFirst();
        return new GxtPanel(panel);
    }

    public GxtPanel(FluentElement panel) {
        this.panel = panel;
    }
    
    public GxtTree tree() {
        return GxtTree.tree(panel.findElement(By.className("x-tree3")));
    }

    public GxtTree treeGrid() {
        return GxtTree.treeGrid(panel.findElement(By.className("x-treegrid")));
    }

    public ToolbarMenu toolbarMenu() {
        return ToolbarMenu.find(panel);
    }
}
