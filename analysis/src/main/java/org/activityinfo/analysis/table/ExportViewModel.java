package org.activityinfo.analysis.table;

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.analysis.TableModel;
import org.activityinfo.model.analysis.table.ExportFormat;

public class ExportViewModel {

    private TableModel tableModel;
    private ExportFormat format;
    private Integer columnLength;
    private boolean columnLimitExceeded;

    public ExportViewModel(TableModel tableModel, ExportFormat format, Integer columnLength, boolean columnLimitExceeded) {
        this.tableModel = tableModel;
        this.format = format;
        this.columnLength = columnLength;
        this.columnLimitExceeded = columnLimitExceeded;
    }

    public TableModel getTableModel() {
        return tableModel;
    }

    public boolean isColumnLimitExceeded() {
        return columnLimitExceeded;
    }

    public boolean isValid() {
        return !columnLimitExceeded;
    }

    public static boolean columnLimitExceeded(EffectiveTableModel effectiveTableModel, ExportFormat format) {
        if (format.hasColumnLimit()) {
            return exportedColumnSize(effectiveTableModel) > format.getColumnLimit();
        }
        return false;
    }

    public static int exportedColumnSize(EffectiveTableModel effectiveTableModel) {
        int count = 0;
        for (EffectiveTableColumn column : effectiveTableModel.getColumns()) {
            count += column.getExportedColumns();
        }
        return count;
    }

    public String getErrorMessage() {
        if (columnLimitExceeded) {
            return I18N.MESSAGES.columnLimit(columnLength, format.getColumnLimit(), format.name());
        }
        return "";
    }
}
