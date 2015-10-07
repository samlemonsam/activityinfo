package org.activityinfo.core.shared.importing.validation;

import com.google.common.collect.Lists;
import org.activityinfo.core.shared.importing.source.SourceRow;

import java.util.List;

public class ValidatedRow {
    private SourceRow row;
    private List<ValidationResult> columns = Lists.newArrayList();

    public ValidatedRow(SourceRow row, List<ValidationResult> columns) {
        this.row = row;
        this.columns = columns;
    }

    public ValidationResult getResult(int columnIndex) {
        return columns.get(columnIndex);
    }

    public SourceRow getSourceRow() {
        return row;
    }

    /**
     * Row is valid if all column's results are valid, otherwise invalid.
     *
     * @return whether row is valid
     */
    public boolean isValid() {
        for (ValidationResult column : columns) {
            if (column.getState() == ValidationResult.State.ERROR && !column.hasReferenceMatch()) {
                return false;
            }
        }
        return true;
    }
}
