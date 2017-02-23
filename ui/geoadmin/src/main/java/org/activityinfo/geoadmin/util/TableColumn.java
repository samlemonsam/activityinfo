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
