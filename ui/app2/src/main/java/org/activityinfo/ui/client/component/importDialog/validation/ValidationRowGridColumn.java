package org.activityinfo.ui.client.component.importDialog.validation;

import com.google.gwt.user.cellview.client.Column;
import org.activityinfo.ui.client.component.importDialog.model.strategy.ColumnAccessor;
import org.activityinfo.ui.client.component.importDialog.model.validation.ValidatedRow;
import org.activityinfo.ui.client.component.importDialog.validation.cells.ValidationResultCell;

public class ValidationRowGridColumn extends Column<ValidatedRow, ValidatedRow> {

    /**
     * Construct a new Column with a given {@link com.google.gwt.cell.client.Cell}.
     *
     * @param accessor
     */
    public ValidationRowGridColumn(ColumnAccessor accessor, int columnIndex) {
        super(new ValidationResultCell(accessor, columnIndex));
    }

    @Override
    public ValidatedRow getValue(ValidatedRow row) {
        return row;
    }
}
