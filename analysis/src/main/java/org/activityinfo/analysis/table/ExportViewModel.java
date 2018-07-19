package org.activityinfo.analysis.table;

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.analysis.TableModel;

public class ExportViewModel {

    public static final int XLS_COLUMN_LIMIT = 256;
    public static final String XLS_EXPORT = "XLS";

    private TableModel tableModel;
    private boolean columnLimitExceeded;

    public ExportViewModel(TableModel tableModel, boolean columnLimitExceeded) {
        this.tableModel = tableModel;
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

    public static boolean columnLimitExceeded(EffectiveTableModel effectiveTableModel) {
        return exportedColumnSize(effectiveTableModel) > XLS_COLUMN_LIMIT;
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
            return I18N.CONSTANTS.columnLimit();
        }
        return "";
    }
}
