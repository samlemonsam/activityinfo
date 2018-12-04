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
package org.activityinfo.ui.client.table.view;

import com.google.common.base.Optional;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.grid.CellSelectionModel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.selection.CellSelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import org.activityinfo.analysis.table.EffectiveTableColumn;
import org.activityinfo.analysis.table.EffectiveTableModel;
import org.activityinfo.analysis.table.TableUpdater;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.SortModel;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;

import java.util.Collections;
import java.util.logging.Logger;


public class TableGrid implements IsWidget, SelectionChangedEvent.HasSelectionChangedHandlers<RecordRef> {

    private static final Logger LOGGER = Logger.getLogger(TableGrid.class.getName());

    private final EffectiveTableModel initialTableModel;
    private TableUpdater tableUpdater;

    private final ListStore<Integer> store;
    private final Grid<Integer> grid;
    private final LiveRecordGridView gridView;

    private Subscription subscription;
    private final ColumnSetProxy proxy;
    private final PagingLoader<PagingLoadConfig, PagingLoadResult<Integer>> loader;

    private final EventBus eventBus = new SimpleEventBus();
    private final TableGridFilters filters;

    public TableGrid(final EffectiveTableModel tableModel, Observable<ColumnSet> columnSet, TableUpdater tableUpdater) {

        this.initialTableModel = tableModel;
        this.tableUpdater = tableUpdater;

        // GXT Grid's are built around row-major data storage, while AI uses
        // Column-major order here. So we construct fake loaders/stores that represent
        // each row as row index.

        proxy = new ColumnSetProxy();
        loader = new PagingLoader<>(proxy);
        loader.setRemoteSort(true);

        store = new ListStore<>(index -> index.toString());

        // Build a grid column model based on the user's selection of columns
        ColumnModelBuilder columns = new ColumnModelBuilder(proxy);
        columns.addAll(tableModel.getColumns());

        gridView = new LiveRecordGridView();
        gridView.setColumnLines(true);
        gridView.setTrackMouseOver(false);
        gridView.addColumnResizeHandler(this::changeColumnWidth);
        gridView.addSortChangeHandler(this::changeSort);

        CellSelectionModel<Integer> sm = new CellSelectionModel<>();
        sm.addCellSelectionChangedHandler(this::changeRowSelection);

        grid = new Grid<Integer>(store, columns.buildColumnModel()) {
            @Override
            protected void onAttach() {
                super.onAttach();
                subscription = columnSet.subscribe(observable -> updateColumnView(observable));
                loader.load(0, gridView.getCacheSize());
            }

            @Override
            protected void onDetach() {
                super.onDetach();
                subscription.unsubscribe();
            }
        };
        grid.setLoader(loader);
        grid.setLoadMask(true);
        grid.setView(gridView);
        grid.setSelectionModel(sm);

        // Setup grid filters
        filters = new TableGridFilters(tableUpdater);
        filters.initPlugin(grid);

        if( !initialTableModel.isSubTable()) {
            for (ColumnView filter : columns.getFilters()) {
                filters.addFilter(filter);
            }
            filters.updateView(tableModel.getFilter());
        }
    }

    private void changeColumnWidth(ColumnResizeEvent e) {

        ColumnConfig<Integer, Object> column = grid.getColumnModel().getColumn(e.getColumnIndex());

        LOGGER.info("Column " + column.getValueProvider().getPath() + " resized to " + e.getColumnWidth() + "px");

        tableUpdater.updateColumnWidth(column.getValueProvider().getPath(), e.getColumnWidth());
    }

    /**
     * Changes the current row selection to the user's new selection
     */
    private void changeRowSelection(CellSelectionChangedEvent<Integer> event) {
        if(proxy.isLoaded()) {
            if(!event.getSelection().isEmpty()) {
                int rowIndex = event.getSelection().get(0).getModel();
                RecordRef selectedRef = new RecordRef(initialTableModel.getFormId(), proxy.getRecordId(rowIndex));
                eventBus.fireEvent(new SelectionChangedEvent<>(Collections.singletonList(selectedRef)));
            }
        }
    }

    /**
     * Changes the current sort order based on the user's input.
     */
    private void changeSort(SortChangeEvent event) {
        Optional<SortModel> sortSelection = Optional.absent();
        if (event.getField().isPresent()) {
            sortSelection = Optional.of(new SortModel(event.getField().get(), event.getDir().get()));
        }
        tableUpdater.updateSort(sortSelection);
        gridView.refresh();
    }

    public boolean updateView(EffectiveTableModel tableModel) {

        // Check to see if we can update columns in place
        if (!tryUpdateColumnsView(tableModel)) {
            LOGGER.info("Columns have changed, rebuild required.");
            return false;
        }

        filters.updateView(tableModel.getFilter());

        return true;
    }

    private boolean tryUpdateColumnsView(EffectiveTableModel tableModel) {
        if(tableModel.getColumns().size() != initialTableModel.getColumns().size()) {
            return false;
        }
        for (int i = 0; i < tableModel.getColumns().size(); i++) {
            EffectiveTableColumn initialColumn = initialTableModel.getColumns().get(i);
            EffectiveTableColumn updatedColumn = tableModel.getColumns().get(i);

            // Check for incompatible changes. Changing the id or the
            // the type of column will rebuilding the grid.
            if (!initialColumn.getId().equals(updatedColumn.getId()) ||
                !initialColumn.getType().equals(updatedColumn.getType())) {
                return false;
            }
            if(!initialColumn.getLabel().equals(updatedColumn.getLabel())) {
                // TODO: update column label in place
                return false;
            }
        }
        gridView.maybeUpdateSortingView(tableModel.getSorting());
        return true;
    }


    private void updateColumnView(Observable<ColumnSet> columnSet) {
        if(!proxy.push(columnSet)) {
            loader.load();
        }
    }

    @Override
    public Widget asWidget() {
        return grid;
    }

    @Override
    public HandlerRegistration addSelectionChangedHandler(SelectionChangedEvent.SelectionChangedHandler<RecordRef> handler) {
        return eventBus.addHandler(SelectionChangedEvent.getType(), handler);
    }

}
