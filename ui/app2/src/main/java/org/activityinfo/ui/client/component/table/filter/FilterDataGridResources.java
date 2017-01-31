package org.activityinfo.ui.client.component.table.filter;
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.DataGrid;

/**
 * @author yuriyz on 4/4/14.
 */
public class FilterDataGridResources implements DataGrid.Resources {

    public static final FilterDataGridResources INSTANCE = new FilterDataGridResources();

    private static final DataGrid.Resources BASE_RESOURCES = GWT.create(DataGrid.Resources.class);

    private static final FilterDataGridStyles STYLE_SHEET = GWT.create(FilterDataGridStyles.class);


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

