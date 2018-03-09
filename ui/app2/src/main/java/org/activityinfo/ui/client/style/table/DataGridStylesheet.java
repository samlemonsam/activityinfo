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

import com.bedatadriven.rebar.style.client.Source;
import com.bedatadriven.rebar.style.client.Strictness;
import com.bedatadriven.rebar.style.client.Stylesheet;
import com.google.gwt.user.cellview.client.DataGrid;

/**
 * Application style sheet for the Data Grid
 */
@Source("datagrid.less")
@Strictness(ignoreMissingClasses = true)
public interface DataGridStylesheet extends Stylesheet, DataGrid.Style {
}
