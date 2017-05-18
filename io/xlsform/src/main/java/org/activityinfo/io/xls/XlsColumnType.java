package org.activityinfo.io.xls;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.time.LocalDate;
import org.apache.poi.ss.usermodel.Cell;

import java.util.Date;

public enum XlsColumnType {
    STRING {
        @Override
        public void setValue(Cell cell, ColumnView view, int row) {
            cell.setCellValue(view.getString(row));
        }
    },
    DATE {
        @Override
        public void setValue(Cell cell, ColumnView view, int row) {
            String dateString = view.getString(row);
            LocalDate localDate = LocalDate.parse(dateString);
            Date date = localDate.atMidnightInMyTimezone();
            cell.setCellValue(date);
        }
    },
    NUMBER {
        @Override
        public void setValue(Cell cell, ColumnView view, int row) {
            cell.setCellValue(view.getDouble(row));
        }
    },
    BOOLEAN {
        @Override
        public void setValue(Cell cell, ColumnView view, int row) {
            cell.setCellValue(view.getBoolean(row) == ColumnView.TRUE);
        }
    },
    EMPTY {
        @Override
        public void setValue(Cell cell, ColumnView view, int row) {
        }
    };

    public abstract void setValue(Cell cell, ColumnView view, int row);

}
