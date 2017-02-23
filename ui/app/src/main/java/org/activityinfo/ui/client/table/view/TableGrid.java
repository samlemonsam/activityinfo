package org.activityinfo.ui.client.table.view;

import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.grid.*;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;
import org.activityinfo.ui.client.table.model.EffectiveColumn;
import org.activityinfo.ui.client.table.model.EffectiveTableModel;

import java.util.ArrayList;
import java.util.List;


public class TableGrid implements IsWidget {

    private final ColumnSetStore store;
    private final Grid<Integer> grid;

    private Subscription subscription;

    public TableGrid(final EffectiveTableModel tableModel) {
        store = new ColumnSetStore(tableModel.getColumnSet());
        List<ColumnConfig<Integer, ?>> columns = new ArrayList<>();
        for (EffectiveColumn tableColumn : tableModel.getColumns()) {
            columns.add(buildColumnConfig(tableColumn));
        }
        ColumnModel<Integer> cm = new ColumnModel<>(columns);

        GridView<Integer> gridView = new GridView<Integer>() {

            private int currentScrollLeft;

            @Override
            protected void syncScroll() {
                int scrollLeft = scroller.getScrollLeft();
                if(currentScrollLeft != scrollLeft) {
                    syncHeaderScroll();
                    currentScrollLeft = scrollLeft;
                }
            }
        };
        gridView.setColumnLines(true);
        gridView.setTrackMouseOver(false);


        grid = new Grid<Integer>(store, cm) {
            @Override
            protected void onAttach() {
                super.onAttach();
                subscription = tableModel.getColumnSet().subscribe(observable -> onColumnsUpdated(observable));
            }

            @Override
            protected void onDetach() {
                super.onDetach();
                subscription.unsubscribe();
            }
        };
        grid.setLoadMask(true);
        grid.setView(gridView);
        grid.setSelectionModel(new CellSelectionModel<>());
    }


    private ColumnConfig<Integer, ?> buildColumnConfig(EffectiveColumn tableColumn) {
        ColumnConfig<Integer, String> columnConfig = new ColumnConfig<>(store.stringValueProvider(tableColumn.getId()));
        columnConfig.setHeader(tableColumn.getLabel());

        if(tableColumn.getType() instanceof QuantityType) {
            columnConfig.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        }

        return columnConfig;
    }

    @Override
    public Widget asWidget() {
        return grid;
    }


    private void onColumnsUpdated(Observable<ColumnSet> columnSet) {
        if(columnSet.isLoading()) {
            grid.mask(I18N.CONSTANTS.loading());
        } else {
            grid.unmask();
        }
    }

    public void update(Observable<EffectiveTableModel> effectiveTable) {

    }
}
