package org.activityinfo.ui.client.component.importDialog.mapping;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.Command;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.CellPreviewEvent.Handler;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.component.importDialog.PageChangedEvent;
import org.activityinfo.ui.client.component.importDialog.model.ColumnAction;
import org.activityinfo.ui.client.component.importDialog.model.IgnoreAction;
import org.activityinfo.ui.client.component.importDialog.model.ImportModel;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceColumn;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceRow;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceTable;
import org.activityinfo.ui.client.style.table.DataGridResources;
import org.activityinfo.ui.client.util.GwtUtil;

import java.util.List;

/**
 * A DataGrid that shows the original columns in the imported table
 * and focuses on helping the user select columns as a whole and map them
 * to existing properties.
 */
public class ColumnMappingGrid extends DataGrid<SourceRow> {

    public static final int SOURCE_COLUMN_HEADER_ROW = 0;
    public static final int MAPPING_HEADER_ROW = 1;

    public static final int COLUMN_WIDTH_LIMIT_IN_EM = 20;

    private final ImportModel model;
    private final EventBus eventBus;

    private SingleSelectionModel<SourceColumn> columnSelectionModel;
    private List<SourceColumn> sourceColumns;

    private final GridHeaderCell headerCell;

    private int lastSelectedColumn = -1;

    public ColumnMappingGrid(ImportModel model, SingleSelectionModel<SourceColumn> columnSelectionModel,
                             EventBus eventBus) {

        super(50, DataGridResources.INSTANCE);

        ColumnMappingStyles.INSTANCE.ensureInjected();
        DataGridResources.INSTANCE.dataGridStyle().ensureInjected();

        this.model = model;
        this.columnSelectionModel = columnSelectionModel;
        this.eventBus = eventBus;

        headerCell = new GridHeaderCell(model);

        this.addStyleName(ColumnMappingStyles.INSTANCE.grid());
        this.setWidth("100%");
        this.setHeight("100%");
        this.setSelectionModel(new NullRowSelectionModel());
        this.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
        this.setHeaderBuilder(new GridHeaderBuilder(this));
        this.setSkipRowHoverCheck(true);
        this.addCellPreviewHandler(new Handler<SourceRow>() {

            @Override
            public void onCellPreview(CellPreviewEvent<SourceRow> event) {
                if (BrowserEvents.CLICK.equals(event.getNativeEvent().getType())) {
                    SourceColumn sourceColumn = sourceColumns.get(event.getColumn());
                    ColumnMappingGrid.this.columnSelectionModel.setSelected(sourceColumn, true);
                }
            }
        });

        this.columnSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                Scheduler.get().scheduleDeferred(new Command() {
                    @Override
                    public void execute() {
                        onColumnSelectionChanged();
                    }
                });
            }
        });

    }

    private void onColumnSelectionChanged() {

        // ensure the column is scrolled into view
        int newColumnIndex = columnSelectionModel.getSelectedObject().getIndex();
        scrollColumnIntoView(newColumnIndex);

        // clear the selection styles from the old column
        if (lastSelectedColumn != -1) {
            removeHeaderStyleName(lastSelectedColumn, ColumnMappingStyles.INSTANCE.selected());
            this.removeColumnStyleName(lastSelectedColumn, ColumnMappingStyles.INSTANCE.selected());
        }

        // add the bg to the new selection
        this.getHeader(newColumnIndex).setHeaderStyleNames(ColumnMappingStyles.INSTANCE.selected());
        this.addColumnStyleName(newColumnIndex, ColumnMappingStyles.INSTANCE.selected());

        lastSelectedColumn = newColumnIndex;
    }

    /**
     * Updates the column styles to match the column's current binding
     */
    public void refreshColumnStyles(int columnIndex) {
        // update the column styles
        final SourceColumn sourceColumn = model.getSourceColumn(columnIndex);
        ColumnAction binding = model.getColumnAction(sourceColumn);

        toggleColumnStyle(columnIndex, ColumnMappingStyles.INSTANCE.stateIgnored(), binding != null &&
                binding == IgnoreAction.INSTANCE);
        toggleColumnStyle(columnIndex, ColumnMappingStyles.INSTANCE.stateBound(), binding != null &&
                binding != IgnoreAction.INSTANCE);

        toggleColumnStyle(columnIndex, ColumnMappingStyles.INSTANCE.stateUnset(), binding == null);

        // update the mapping description
        Cell.Context context = new Cell.Context(MAPPING_HEADER_ROW, columnIndex, null);
        SafeHtmlBuilder html = new SafeHtmlBuilder();
        headerCell.render(context, sourceColumn, html);

        getTableHead(MAPPING_HEADER_ROW, columnIndex).setInnerSafeHtml(html.toSafeHtml());
    }

    private void scrollColumnIntoView(int selectedColumnIndex) {
        Element td = this.getRowElement(0).getChild(selectedColumnIndex).cast();
        td.scrollIntoView();
    }

    private void addHeaderStyleName(int columnIndex, String className) {
        getTableHead(SOURCE_COLUMN_HEADER_ROW, columnIndex).addClassName(className);
        getTableHead(MAPPING_HEADER_ROW, columnIndex).addClassName(className);
    }

    private void removeHeaderStyleName(int columnIndex, String className) {
        getTableHead(SOURCE_COLUMN_HEADER_ROW, columnIndex).removeClassName(className);
        getTableHead(MAPPING_HEADER_ROW, columnIndex).removeClassName(className);
    }

    private Element getTableHead(int rowIndex, int columnIndex) {
        final TableRowElement row = getTableHeadElement().getRows().getItem(rowIndex);
        return row.getCells().getItem(columnIndex);
    }

    private void toggleColumnStyle(int index, String className, boolean enabled) {
        if (enabled) {
            this.addColumnStyleName(index, className);
            addHeaderStyleName(index, className);

        } else {
            this.removeColumnStyleName(index, className);
            removeHeaderStyleName(index, className);
        }
    }

    private static int columnWidthInEm(String header) {
        int width = GwtUtil.columnWidthInEm(header);
        if (width > COLUMN_WIDTH_LIMIT_IN_EM) {
            return COLUMN_WIDTH_LIMIT_IN_EM;
        }
        return width;
    }

    public void refresh() {
        while (this.getColumnCount() > 0) {
            this.removeColumn(0);
        }
        sourceColumns = model.getSource().getColumns();
        for (SourceColumn sourceColumn : sourceColumns) {
            GridColumn gridColumn = new GridColumn(sourceColumn);
            GridHeader gridHeader = new GridHeader(sourceColumn, headerCell, columnSelectionModel);
            addColumn(gridColumn, gridHeader);
            setColumnWidth(gridColumn, columnWidthInEm(sourceColumn.getHeader()), com.google.gwt.dom.client.Style.Unit.EM);
        }
        this.redrawHeaders();

        // show already parsed data (it can be only part of it, not all)
        this.setRowData(model.getSource().getRows());

        // update table if not all rows are parsed
        if (!model.getSource().parsedAllRows()) {
            // give some time to switch the page
            Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
                @Override
                public boolean execute() {
                    refreshWithNewlyParsedRows();
                    return false;
                }
            }, 2000); // wait 2 seconds
        }
    }

    private void refreshWithNewlyParsedRows() {
        eventBus.fireEvent(new PageChangedEvent(false, I18N.CONSTANTS.parsingRows()));
        Scheduler.get().scheduleFixedPeriod(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                SourceTable sourceTable = model.getSource();
                if (!sourceTable.parsedAllRows()) {
                    sourceTable.parseNextRows(50);
                    ColumnMappingGrid.this.setRowData(model.getSource().getRows());
                    ColumnMappingGrid.this.getRowElement(ColumnMappingGrid.this.getRowCount() - 1).scrollIntoView();
                } else {
                    ColumnMappingGrid.this.scrollColumnIntoView(0);
                    eventBus.fireEvent(new PageChangedEvent(true, ""));
                    return false;
                }
                return true;
            }
        }, 1);
    }
}
