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

import org.activityinfo.i18n.shared.I18N;

import java.util.Arrays;
import java.util.List;

/**
 * @author yuriyz on 06/25/2015.
 */
public class TableFilterDialog {

    private BsModal modal;

    public TableFilterDialog(BsModal modal) {
        this.modal = modal;
    }

    public BsTable table() {
        return new BsTable(modal.getWindowElement().find().table().first(), BsTable.Type.GRID_TABLE);
    }

    public TableFilterDialog select(String... filterValues) {
        return select(Arrays.asList(filterValues));
    }

    public TableFilterDialog select(List<String> filterValues) {
        for (String filterValue : filterValues) {
            selectFilterValue(filterValue);
        }
        return this;
    }

    private void selectFilterValue(String filterValue) {
        BsTable.Cell cell = table().waitForCellByText(filterValue);
        // get sibling td checkbox and click on it
        cell.getContainer().find().precedingSibling().td().descendants().input().clickWhenReady();
    }

    public TableFilterDialog apply() {
        modal.click(I18N.CONSTANTS.ok());
        return this;
    }
}
