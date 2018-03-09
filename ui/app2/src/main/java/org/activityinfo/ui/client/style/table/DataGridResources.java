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
package org.activityinfo.ui.client.style.table;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.DataGrid;

/**
 * Our own Application resources for the GWT data grid
 */
public class DataGridResources implements DataGrid.Resources {

    public static final DataGridResources INSTANCE = new DataGridResources();

    private static final DataGrid.Resources BASE_RESOURCES = GWT.create(DataGrid.Resources.class);

    private static final DataGridStylesheet STYLE_SHEET = GWT.create(DataGridStylesheet.class);


    @Override
    public ImageResource dataGridLoading() {
        return BASE_RESOURCES.dataGridLoading();
    }

    @Override
    public ImageResource dataGridSortAscending() {
        return BASE_RESOURCES.dataGridSortDescending();
    }

    @Override
    public ImageResource dataGridSortDescending() {
        return BASE_RESOURCES.dataGridSortDescending();
    }

    @Override
    public DataGrid.Style dataGridStyle() {
        return STYLE_SHEET;
    }
}
