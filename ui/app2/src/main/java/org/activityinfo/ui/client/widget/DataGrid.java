package org.activityinfo.ui.client.widget;
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

import com.google.gwt.dom.client.TableSectionElement;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ProvidesKey;

/**
 * DataGrid with public access to getTableHeadElement()
 *
 * @author yuriyz on 4/3/14.
 */
public class DataGrid<T> extends com.google.gwt.user.cellview.client.DataGrid<T> {

    public DataGrid() {
    }

    public DataGrid(int pageSize) {
        super(pageSize);
    }

    public DataGrid(int pageSize, ProvidesKey<T> keyProvider) {
        super(pageSize, keyProvider);
    }

    public DataGrid(int pageSize, Resources resources) {
        super(pageSize, resources);
    }

    public DataGrid(int pageSize, Resources resources, ProvidesKey<T> keyProvider) {
        super(pageSize, resources, keyProvider);
    }

    public DataGrid(int pageSize, Resources resources, ProvidesKey<T> keyProvider, Widget loadingIndicator) {
        super(pageSize, resources, keyProvider, loadingIndicator);
    }

    public DataGrid(ProvidesKey<T> keyProvider) {
        super(keyProvider);
    }

    @Override
    public TableSectionElement getTableHeadElement() {
        return super.getTableHeadElement();
    }
}
