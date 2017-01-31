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

import com.bedatadriven.rebar.style.client.Source;
import com.bedatadriven.rebar.style.client.Strictness;
import com.bedatadriven.rebar.style.client.Stylesheet;
import com.google.gwt.user.cellview.client.DataGrid;

/**
 * @author yuriyz on 4/4/14.
 */
@Source("filterdatagrid.less")
@Strictness(ignoreMissingClasses = true)
public interface FilterDataGridStyles extends Stylesheet, DataGrid.Style {

    @ClassName("filter-panel")
    String filterPanel();
}
