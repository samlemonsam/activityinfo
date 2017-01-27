package org.activityinfo.ui.client.table.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Observer;
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

        grid = new Grid<Integer>(store, cm) {
            @Override
            protected void onAttach() {
                super.onAttach();
                subscription = tableModel.getColumnSet().subscribe(new Observer<ColumnSet>() {
                    @Override
                    public void onChange(Observable<ColumnSet> observable) {
                        onColumnsUpdated(observable);
                    }
                });
            }

            @Override
            protected void onDetach() {
                super.onDetach();

            }
        };
        grid.setLoadMask(true);
        grid.getView().setColumnLines(true);
    }


    private ColumnConfig<Integer, ?> buildColumnConfig(EffectiveColumn tableColumn) {
        ColumnConfig<Integer, String> columnConfig = new ColumnConfig<>(store.stringValueProvider(tableColumn.getId()));
        columnConfig.setHeader(tableColumn.getLabel());
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
