package org.activityinfo.ui.client.table.view;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.event.SortChangeEvent;
import com.sencha.gxt.widget.core.client.grid.CellSelectionModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.selection.CellSelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import org.activityinfo.analysis.table.EffectiveTableColumn;
import org.activityinfo.analysis.table.EffectiveTableModel;
import org.activityinfo.analysis.table.TableUpdater;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;

import java.util.Collections;
import java.util.logging.Logger;


public class TableGrid implements IsWidget, SelectionChangedEvent.HasSelectionChangedHandlers<RecordRef> {

    private static final Logger LOGGER = Logger.getLogger(TableGrid.class.getName());

    private final EffectiveTableModel initialTableModel;

    private final ListStore<Integer> store;
    private final Grid<Integer> grid;

    private Subscription subscription;
    private final ColumnSetProxy proxy;
    private final PagingLoader<PagingLoadConfig, PagingLoadResult<Integer>> loader;

    private final EventBus eventBus = new SimpleEventBus();
    private final TableGridFilters filters;

    public TableGrid(final EffectiveTableModel tableModel, Observable<ColumnSet> columnSet, TableUpdater tableUpdater) {

        this.initialTableModel = tableModel;

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

        LiveRecordGridView gridView = new LiveRecordGridView();
        gridView.setColumnLines(true);
        gridView.setTrackMouseOver(false);

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
        grid.addSortChangeHandler(this::changeSort);

        // Setup grid filters
        filters = new TableGridFilters(tableUpdater);
        filters.initPlugin(grid);
        for (ColumnView filter : columns.getFilters()) {
            filters.addFilter(filter);
        }
        filters.updateView(tableModel.getFilter());
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
        // TODO
    }

    public boolean updateView(EffectiveTableModel tableModel) {

        // Check to see if we can update columns in place
        if (!tryUpdateColumns(tableModel)) {
            LOGGER.info("Columns have changed, rebuild required.");
            return false;
        }

        filters.updateView(tableModel.getFilter());

        return true;
    }

    private boolean tryUpdateColumns(EffectiveTableModel tableModel) {
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
        return true;
    }

    private void updateColumnView(Observable<ColumnSet> columnSet) {
        if(columnSet.isLoaded()) {
            if(!proxy.push(columnSet.get())) {
                loader.load();
            }
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
