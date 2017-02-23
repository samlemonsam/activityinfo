package org.activityinfo.geoadmin.util;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class GenericTableModel<T> extends AbstractTableModel {

	private List<T> rows;
	private List<TableColumn<T, ?>> columns;

	public GenericTableModel(List<T> rows, List<TableColumn<T, ?>> columns) {
		super();
		this.rows = rows;
		this.columns = columns;
	}

	@Override
	public int getColumnCount() {
		return columns.size();
	}

	@Override
	public int getRowCount() {
		return rows.size();
	}

	public T getRowAt(int row) {
		return rows.get(row);
	}
	
	@Override
	public Object getValueAt(int row, int column) {
		return columns.get(column).getValue(rows.get(row));
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return columns.get(columnIndex).getColumnClass();
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columns.get(columnIndex).getName();
	}

	public static <RowT> Builder<RowT> newModel(List<RowT> rows) {
		Builder<RowT> builder = new Builder<RowT>();
		builder.rows = rows;
		return builder;
	}
	
	public static class Builder<RowT> {
		private List<RowT> rows;
		private List<TableColumn<RowT, ?>> columns = Lists.newArrayList();
		
		public <ValueT> Builder<RowT> addColumn(String name, Class<ValueT> clazz, Function<RowT, ValueT> accessor) {
			columns.add(new TableColumn<RowT, ValueT>(name, clazz, accessor));
			return this;
		}
		
		public GenericTableModel<RowT> build() {
			return new GenericTableModel<RowT>(rows, columns);
		}
		
	}
	
	
	
	
}
