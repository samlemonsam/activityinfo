package org.activityinfo.ui.client.component.importDialog.mapping;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.view.client.SelectionModel;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceColumn;

/**
 * Header for a SourceColumn
 */
class GridHeader extends Header<SourceColumn> {
    private SourceColumn column;
    private SelectionModel<SourceColumn> columnSelectionModel;

    public GridHeader(SourceColumn column, GridHeaderCell cell,
                      SelectionModel<SourceColumn> columnSelectionModel) {
        super(cell);
        this.column = column;
        this.columnSelectionModel = columnSelectionModel;
    }

    @Override
    public SourceColumn getValue() {
        return column;
    }

    @Override
    public void onBrowserEvent(Cell.Context context, Element elem, NativeEvent event) {
        columnSelectionModel.setSelected(column, true);
    }


}
