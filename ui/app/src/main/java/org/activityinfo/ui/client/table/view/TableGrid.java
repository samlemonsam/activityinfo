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
import com.sencha.gxt.widget.core.client.grid.CellSelectionModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.filters.Filter;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import org.activityinfo.analysis.table.EffectiveTableColumn;
import org.activityinfo.analysis.table.EffectiveTableModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;

import java.util.Collections;


public class TableGrid implements IsWidget, SelectionChangedEvent.HasSelectionChangedHandlers<RecordRef> {

    private final ListStore<Integer> store;
    private final Grid<Integer> grid;

    private Subscription subscription;
    private final ColumnSetProxy proxy;
    private final PagingLoader<PagingLoadConfig, PagingLoadResult<Integer>> loader;

    private final EventBus eventBus = new SimpleEventBus();

    public TableGrid(final EffectiveTableModel tableModel) {

        // GXT Grid's are built around row-major data storage, while AI uses
        // Column-major order here. So we construct fake loaders/stores that represent
        // each row as row index.

        proxy = new ColumnSetProxy();
        loader = new PagingLoader<>(proxy);
        loader.setRemoteSort(true);

        store = new ListStore<>(index -> index.toString());

        // Build a grid column model based on the user's selection of columns
        GridColumnModelBuilder columns = new GridColumnModelBuilder(proxy);
        columns.addAll(tableModel.getColumns());

        LiveRecordGridView gridView = new LiveRecordGridView();
        gridView.setColumnLines(true);
        gridView.setTrackMouseOver(false);

        CellSelectionModel<Integer> sm = new CellSelectionModel<>();
        sm.addCellSelectionChangedHandler(event -> {
            if(proxy.isLoaded()) {
                if(!event.getSelection().isEmpty()) {
                    int rowIndex = event.getSelection().get(0).getModel();
                    RecordRef selectedRef = new RecordRef(tableModel.getFormId(), proxy.getRecordId(rowIndex));
                    eventBus.fireEvent(new SelectionChangedEvent<>(Collections.singletonList(selectedRef)));
                }
            }
        });

        grid = new Grid<Integer>(store, columns.buildColumnModel()) {
            @Override
            protected void onAttach() {
                super.onAttach();
                subscription = tableModel.getColumnSet().subscribe(observable -> onColumnsUpdated(observable));
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
        TableGridFilters filters = new TableGridFilters();
        filters.initPlugin(grid);
        for (Filter<Integer, ?> filter : columns.getFilters()) {
            filters.addFilter(filter);
        }
    }


    @Override
    public Widget asWidget() {
        return grid;
    }


    private void onColumnsUpdated(Observable<ColumnSet> columnSet) {
        if(columnSet.isLoaded()) {
            if(!proxy.push(columnSet.get())) {
                loader.load();
            }
        }
    }


    @Override
    public HandlerRegistration addSelectionChangedHandler(SelectionChangedEvent.SelectionChangedHandler<RecordRef> handler) {
        return eventBus.addHandler(SelectionChangedEvent.getType(), handler);

    }


}
