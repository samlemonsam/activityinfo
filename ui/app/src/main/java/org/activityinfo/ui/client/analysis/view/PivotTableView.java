package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import org.activityinfo.ui.client.analysis.model.AnalysisModel;
import org.activityinfo.ui.client.analysis.model.Point;

import java.util.Arrays;

/**
 * Created by alex on 30-1-17.
 */
public class PivotTableView implements IsWidget {

    private final AnalysisModel model;
    private final ListStore<Point> store;
    private ContentPanel panel;
    private Grid grid;

    public PivotTableView(AnalysisModel model) {
        this.model = model;
        this.store = new ListStore<Point>(point -> point.toString());

        ColumnConfig<Point, Double> valueColumn = new ColumnConfig<Point, Double>(new ValueProvider<Point, Double>() {
            @Override
            public Double getValue(Point object) {
                return object.getValue();
            }

            @Override
            public void setValue(Point object, Double value) {

            }

            @Override
            public String getPath() {
                return "value";
            }
        });

        ColumnModel<Point> columnModel = new ColumnModel<>(Arrays.asList(valueColumn));

        this.grid = new Grid(store, columnModel);
        this.panel = new ContentPanel();
        this.panel.add(grid);
    }

    @Override
    public Widget asWidget() {
        return panel;
    }
}
