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

import com.google.api.client.util.Lists;
import com.google.common.base.Predicate;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.pageobject.api.FluentElements;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author yuriyz on 06/23/2015.
 */
public class ChooseColumnsDialog {

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

    private List<BsTable.Row> rows(boolean isVisible) {
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

    public ChooseColumnsDialog showAllColumns() {
        for (final BsTable.Row row : rows(false)) {
            final String text = row.getContainer().text();
            row.getCells().get(0).getContainer().doubleClick();

            modal.getWindowElement().waitUntil(new Predicate<WebDriver>() {
                @Override
                public boolean apply(@Nullable WebDriver input) {
                    // if not in invisible row list then stop waiting
                    for (BsTable.Row notVisible : rows(false)) {
                        if (notVisible.getContainer().text().contains(text)) {
                            return false;
                        }
                    }
                    return true;
                }
            });
        }
        return this;
    }
}
