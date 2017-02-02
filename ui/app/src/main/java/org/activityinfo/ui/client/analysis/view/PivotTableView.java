package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Observer;
import org.activityinfo.ui.client.analysis.model.AnalysisModel;
import org.activityinfo.ui.client.analysis.model.AnalysisResult;
import org.activityinfo.ui.client.analysis.model.DimensionSet;
import org.activityinfo.ui.client.analysis.model.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PivotTableView implements IsWidget {

    private static final Logger LOGGER = Logger.getLogger(PivotTableView.class.getName());

    private final AnalysisModel model;
    private ListStore<Point> store;
    private ContentPanel panel;
    private Grid<Point> grid;

    public PivotTableView(AnalysisModel model) {
        this.model = model;
        this.store = new ListStore<>(point -> point.toString());
        this.grid = new Grid<>(store, buildColumnModel(new DimensionSet()));
        this.grid.getView().setSortingEnabled(false);
        this.panel = new ContentPanel();
        this.panel.setHeading("Results");
        this.panel.add(grid);

        model.getResult().subscribe(observable -> {
            if (observable.isLoaded()) {
                update(observable.get());
            } else {
                store.clear();
            }
        });
    }

    private void update(AnalysisResult analysisResult) {
        grid.reconfigure(store, buildColumnModel(analysisResult.getDimensionSet()));
        store.replaceAll(analysisResult.getPoints());
    }

    private ColumnModel<Point> buildColumnModel(DimensionSet dimensionSet) {
        List<ColumnConfig<Point, ?>> columns = new ArrayList<>();
        for (int i = 0; i < dimensionSet.getCount(); i++) {
            ColumnConfig<Point, String> column = new ColumnConfig<>(new PointDimProvider(i));
            column.setHeader(dimensionSet.getDimension(i).getLabel());
            column.setSortable(false);
            column.setHideable(false);
            columns.add(column);
        }

        ColumnConfig<Point, Double> valueColumn = new ColumnConfig<>(new PointValueProvider());
        valueColumn.setHeader(I18N.CONSTANTS.value());
        valueColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LOCALE_END);
        valueColumn.setSortable(false);
        valueColumn.setHideable(false);
        columns.add(valueColumn);

        return new ColumnModel<>(columns);
    }

    @Override
    public Widget asWidget() {
        return panel;
    }
}
