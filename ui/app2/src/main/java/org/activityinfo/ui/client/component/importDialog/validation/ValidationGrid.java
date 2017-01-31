package org.activityinfo.ui.client.component.importDialog.validation;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.ui.ResizeComposite;
import org.activityinfo.ui.client.component.importDialog.model.strategy.FieldImporterColumn;
import org.activityinfo.ui.client.component.importDialog.model.validation.ValidatedRow;
import org.activityinfo.ui.client.component.importDialog.model.validation.ValidatedRowTable;
import org.activityinfo.ui.client.style.table.DataGridResources;
import org.activityinfo.ui.client.util.GwtUtil;

/**
 * A second DataGrid that allows the user to the resolve
 * any problems before import
 */
public class ValidationGrid extends ResizeComposite {

    private DataGrid<ValidatedRow> dataGrid;

    public ValidationGrid() {
        this.dataGrid = new DataGrid<>(100, DataGridResources.INSTANCE);
        initWidget(dataGrid);
        this.dataGrid.setWidth("100%");
        this.dataGrid.setHeight("100%");
    }

    public void refresh(ValidatedRowTable table) {
        while (dataGrid.getColumnCount() > 0) {
            dataGrid.removeColumn(0);
        }
        for(int i = 0; i< table.getColumns().size(); i++) {
            final FieldImporterColumn column = table.getColumns().get(i);
            String columnHeader = column.getAccessor().getHeading();
            dataGrid.addColumn(new ValidationRowGridColumn(column.getAccessor(), i),
                    new TextHeader(columnHeader));
            dataGrid.setColumnWidth(i, GwtUtil.columnWidthInEm(columnHeader), Style.Unit.EM);
        }
        dataGrid.setRowData(table.getRows());
    }

    public int getInvalidRowsCount() {
        int invalidRowsCount = 0;
        for (ValidatedRow row : dataGrid.getVisibleItems()) {
            if (!row.isValid()) {
                invalidRowsCount++;
            }
        }
        return invalidRowsCount;
    }
}
