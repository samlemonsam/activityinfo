package org.activityinfo.test.pageobject.bootstrap;
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


import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.api.FluentElements;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * @author yuriyz on 06/23/2015.
 */
public class ChooseColumnsDialog {

    public static final List<String> BUILT_IN_COLUMNS = Arrays.asList(
         I18N.CONSTANTS.startDate(), I18N.CONSTANTS.endDate(),
            I18N.CONSTANTS.partner() + " " + I18N.CONSTANTS.name(),
            I18N.CONSTANTS.project() + " " + I18N.CONSTANTS.name(),
            I18N.CONSTANTS.partner() + " " + I18N.CONSTANTS.fullName(), I18N.CONSTANTS.partner()
    );

    private BsModal modal;

    public ChooseColumnsDialog(BsModal modal) {
        this.modal = modal;
    }

    private FluentElements tables() {
        return modal.getWindowElement().findElements(By.tagName("table"));
    }

    private BsTable allColumnsGrid() {
        return new BsTable(tables().get(0), BsTable.Type.GRID_TABLE);
    }

    private BsTable visibleColumnsGrid() {
        return new BsTable(tables().get(1), BsTable.Type.GRID_TABLE);
    }

    private List<BsTable.Row> allColumnsRows(boolean isVisible) {
        BsTable allColumnsGrid = allColumnsGrid();
        List<BsTable.Row> visible = Lists.newArrayList();
        List<BsTable.Row> notVisible = Lists.newArrayList();

        for (BsTable.Row row : allColumnsGrid.rows()) {
            if (StringUtils.contains(row.getContainer().attribute("class"), "row-present")) {
                visible.add(row);
            } else {
                notVisible.add(row);
            }
        }
        return isVisible ? visible : notVisible;
    }

    public ChooseColumnsDialog ok() {
        modal.click(I18N.CONSTANTS.ok());
        return this;
    }

    private BsTable.Row getFirstVisibleBuiltInRow() {
        for (BsTable.Row row : visibleColumnsGrid().rows()) {
            if (BUILT_IN_COLUMNS.contains(row.getContainer().text())) {
                return row;
            }
        }
        return null;
    }

    public ChooseColumnsDialog hideBuiltInColumns() {
        int counter = 0;

        while (getFirstVisibleBuiltInRow() != null) {
            // first select first visible row
            getFirstVisibleBuiltInRow().getContainer().clickWhenReady();

            // element is changed because of selection (stale element), re-select invisible rows again
            FluentElement container = getFirstVisibleBuiltInRow().getContainer();
            final String text = container.text();
            container.doubleClick();

            modal.getWindowElement().waitUntil(new Predicate<WebDriver>() {
                @Override
                public boolean apply(@Nullable WebDriver input) {
                    // wait until appears in invisible list
                    for (BsTable.Row invisible : allColumnsRows(false)) {
                        if (invisible.getContainer().text().contains(text)) {
                            return false;
                        }
                    }
                    return true;
                }
            });
            counter++;
            if (counter > 1000) { // be on safe side
                throw new AssertionError("Failed to make all columns visible in instance table.");
            }
        }
        return this;
    }

    public ChooseColumnsDialog showAllColumns() {
        int counter = 0;

        while (!allColumnsRows(false).isEmpty()) {
            // first select first invisible row
            allColumnsRows(false).get(0).getContainer().clickWhenReady();

            // element is changed because of selection (stale element), re-select invisible rows again
            FluentElement container = allColumnsRows(false).get(0).getContainer();
            final String text = container.text();
            container.doubleClick();

            modal.getWindowElement().waitUntil(new Predicate<WebDriver>() {
                @Override
                public boolean apply(@Nullable WebDriver input) {
                    // if not in invisible row list then stop waiting
                    for (BsTable.Row notVisible : allColumnsRows(false)) {
                        if (notVisible.getContainer().text().contains(text)) {
                            return false;
                        }
                    }
                    return true;
                }
            });
            counter++;
            if (counter > 1000) { // be on safe side
                throw new AssertionError("Failed to make all columns visible in instance table.");
            }
        }
        return this;
    }

    public BsModal getModal() {
        return modal;
    }
}
