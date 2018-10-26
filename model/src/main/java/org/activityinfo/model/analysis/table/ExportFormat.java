package org.activityinfo.model.analysis.table;

import javax.annotation.Nullable;

public enum ExportFormat {

    CSV(null),
    XLS(256);

    private Integer columnLimit;

    ExportFormat(@Nullable Integer columnLimit) {
        this.columnLimit = columnLimit;
    }

    public boolean hasColumnLimit() {
        return columnLimit != null;
    }

    public Integer getColumnLimit() {
        assert hasColumnLimit();
        return columnLimit;
    }

}
