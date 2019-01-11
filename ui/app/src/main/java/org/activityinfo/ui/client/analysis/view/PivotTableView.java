/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.grid.*;
import com.sencha.gxt.widget.core.client.info.DefaultInfoConfig;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.info.InfoConfig;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.analysis.pivot.viewModel.AnalysisViewModel;
import org.activityinfo.analysis.pivot.viewModel.EffectiveDimension;
import org.activityinfo.analysis.pivot.viewModel.PivotTable;
import org.activityinfo.analysis.pivot.viewModel.PivotTableRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class PivotTableView implements IsWidget {

    private static final Logger LOGGER = Logger.getLogger(PivotTableView.class.getName());

    private final AnalysisViewModel model;
    private ListStore<PivotRow> store;
    private ContentPanel panel;
    private Grid<PivotRow> grid;

    private final TextButton saveButton;
    private final TextButton copyButton;
    private final TextButton exportButton;

    public PivotTableView(AnalysisViewModel model) {
        this.model = model;
        this.store = new ListStore<>(point -> point.toString());

        saveButton = new TextButton(I18N.CONSTANTS.save());

        copyButton = new TextButton(I18N.CONSTANTS.copy());
        copyButton.addSelectHandler(this::copyTable);

        exportButton = new TextButton(I18N.CONSTANTS.export());
        exportButton.addSelectHandler(this::exportTable);

        ToolBar toolbar = new ToolBar();
        toolbar.add(saveButton);
        toolbar.add(copyButton);
        toolbar.add(exportButton);

        this.grid = new Grid<>(store, buildColumnModel(new PivotTable()));
        this.grid.getView().setSortingEnabled(false);
        this.grid.setSelectionModel(new CellSelectionModel<>());

        VerticalLayoutContainer container = new VerticalLayoutContainer();
        container.add(toolbar, new VerticalLayoutContainer.VerticalLayoutData(1, -1));
        container.add(grid, new VerticalLayoutContainer.VerticalLayoutData(-1, -1));

        this.panel = new ContentPanel();
        this.panel.setHeading("Results");
        this.panel.add(container);

        model.getPivotTable().subscribe(pivotTable -> {
            boolean ready = pivotTable.isLoaded() && !pivotTable.get().isEmpty();
            copyButton.setEnabled(ready);
            exportButton.setEnabled(ready);

            if (pivotTable.isLoaded()) {
                update(pivotTable.get());
            } else {
                store.clear();
            }
        });
    }

    private void openSavedReports() {
        Window.Location.assign("?ui=3");
    }


    private void copyTable(SelectEvent event) {
        String table = PivotTableRenderer.renderDelimited(model.getPivotTable().get(), "\t");
        if(Clipboard.copy(table)) {
            DefaultInfoConfig config = new DefaultInfoConfig(I18N.CONSTANTS.copied(),
                I18N.CONSTANTS.copiedToClipboard());
            config.setPosition(InfoConfig.InfoPosition.BOTTOM_RIGHT);
            config.setDisplay(1000);
            Info.display(config);
        }
    }

    private void exportTable(SelectEvent event) {
        String table = PivotTableRenderer.renderDelimited(model.getPivotTable().get(), ",");
        OfflineExporter.export("Export.csv", table, OfflineExporter.CSV_MIMETYPE);
    }

    public TextButton getSaveButton() {
        return saveButton;
    }

    private void update(PivotTable pivotTable) {
        grid.reconfigure(store, buildColumnModel(pivotTable));
        store.replaceAll(buildRows(pivotTable));
    }

    private ColumnModel<PivotRow> buildColumnModel(PivotTable pivotTable) {

        List<ColumnConfig<PivotRow, ?>> columns = new ArrayList<>();

        List<EffectiveDimension> rowDimensions = pivotTable.getRowDimensions();
        for (int i = 0; i < rowDimensions.size(); i++) {
            EffectiveDimension rowDim = rowDimensions.get(i);
            ColumnConfig<PivotRow, String> column = new ColumnConfig<>(new PivotRowHeaderProvider(i));
            column.setSortable(false);
            column.setHideable(false);
            column.setHeader(rowDim.getLabel());
            column.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
            columns.add(column);
        }

        List<PivotTable.Node> leafColumns = pivotTable.getRootColumn().getLeaves();
        for (PivotTable.Node leafColumn : leafColumns) {
            ColumnConfig<PivotRow, String> column = new ColumnConfig<>(new PivotValueProvider(leafColumn));
            if(leafColumn.getCategoryLabel() == null) {
                column.setHeader(I18N.CONSTANTS.value());
            } else {
                column.setHeader(leafColumn.getCategoryLabel());
            }
            column.setSortable(false);
            column.setHideable(false);
            column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LOCALE_END);
            columns.add(column);
        }

        ColumnModel<PivotRow> columnModel = new ColumnModel<>(columns);

        if(pivotTable.getColumnDimensions().size() > 0) {
            int startRow = 0;
            int startCol = pivotTable.getRowDimensions().size();
            addHeaderGroups(columnModel, pivotTable.getRootColumn(), startRow, startCol);
        }
        return columnModel;
    }

    private int addHeaderGroups(ColumnModel<PivotRow> cm, PivotTable.Node parent, int row, int col) {
        int leafCount = parent.getLeaves().size();

        // Add one group for the name of this Dimension
        cm.addHeaderGroup(row, col, new HeaderGroupConfig(parent.getDimension().getLabel(), 1, leafCount));


        for (PivotTable.Node child : parent.getChildren()) {
            if (child.isLeaf()) {
                col++;
            } else {
                cm.addHeaderGroup(row + 1, col,
                        new HeaderGroupConfig(child.getCategoryLabel(), 1, child.getLeaves().size()));
                col = addHeaderGroups(cm, child, row + 2, col);
            }
        }
        return col;
    }

    private List<PivotRow> buildRows(PivotTable table) {
        List<PivotRow> list = new ArrayList<>();
        String[] rowHeaders = new String[table.getRowDimensions().size()];

        addRows(list, table.getRootRow(), rowHeaders, 0);

        return list;

    }

    private void addRows(List<PivotRow> list, PivotTable.Node parent, String[] rowHeaders, int rowHeaderIndex) {

        if(parent.isLeaf()) {
            list.add(new PivotRow(Arrays.copyOf(rowHeaders, rowHeaders.length), parent));
        } else {
            for (PivotTable.Node child : parent.getChildren()) {
                rowHeaders[rowHeaderIndex] = child.getCategoryLabel();
                addRows(list, child, rowHeaders, rowHeaderIndex+1);
                if(rowHeaderIndex > 0) {
                    rowHeaders[rowHeaderIndex - 1] = null;
                }
            }
        }
    }

    @Override
    public Widget asWidget() {
        return panel;
    }
}
