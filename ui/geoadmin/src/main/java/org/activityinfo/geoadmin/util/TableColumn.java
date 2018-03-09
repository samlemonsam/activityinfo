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
package org.activityinfo.geoadmin.util;

import com.google.common.base.Function;

public class TableColumn<RowT, ValueT> {

	private String name;
	private Class<ValueT> columnClass;
	private Function<RowT, ValueT> accessor;
	
	TableColumn(String name, Class<ValueT> columnClass,
			Function<RowT, ValueT> accessor) {
		super();
		this.name = name;
		this.columnClass = columnClass;
		this.accessor = accessor;
	}

	public String getName() {
		return name;
	}

	public Class<ValueT> getColumnClass() {
		return columnClass;
	}

	public Function<RowT, ValueT> getAccessor() {
		return accessor;
	}
	
	public ValueT getValue(RowT row) {
		return accessor.apply(row);
	}
	
	
}
