package org.activityinfo.io.xls;

import org.activityinfo.analysis.table.EffectiveTableColumn;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

/**
 * Represents a single column
 */
public class XlsColumn {
    private final EffectiveTableColumn tableColumn;
    private final int columnIndex;
    private final XlsColumnType columnType;
    private final CellStyle style;
    private final ColumnView view;

    public XlsColumn(EffectiveTableColumn tableColumn, ColumnSet columnSet, int columnIndex, CellStyle style) {

        this.tableColumn = tableColumn;
        this.columnIndex = columnIndex;
        this.columnType = XlsColumnTypeFactory.get(tableColumn.getType());
        this.style = style;
        this.view = columnSet.getColumnView(tableColumn.getId());
    }

    public String getHeading() {
        return tableColumn.getLabel();
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public boolean isMissing(int row) {
        return view.isMissing(row);
    }

    /**
     * Sets the cell's value to this column's value at {@code rowIndex}
     */
    public void setValue(Cell cell, int row) {
        columnType.setValue(cell, view, row);
    }

    public CellStyle getStyle() {
        return style;
    }
}
