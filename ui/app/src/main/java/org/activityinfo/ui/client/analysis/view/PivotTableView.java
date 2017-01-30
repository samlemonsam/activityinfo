package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Observer;
import org.activityinfo.ui.client.analysis.model.AnalysisModel;
import org.activityinfo.ui.client.analysis.model.AnalysisResult;
import org.activityinfo.ui.client.analysis.model.Point;

import java.util.Arrays;
import java.util.logging.Level;
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

        ColumnConfig<Point, Double> valueColumn = new ColumnConfig<>(new ValueProvider<Point, Double>() {
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
        valueColumn.setHeader("Value");
        valueColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LOCALE_END);

        ColumnModel<Point> columnModel = new ColumnModel<>(Arrays.asList(valueColumn));

        this.grid = new Grid<>(store, columnModel);
        this.panel = new ContentPanel();
        this.panel.add(grid);

        try {
            model.getResult().subscribe(new Observer<AnalysisResult>() {
                @Override
                public void onChange(Observable<AnalysisResult> observable) {
                    if (observable.isLoaded()) {
                        store.replaceAll(observable.get().getPoints());
                    } else {
                        store.clear();
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to result", e);
        }
    }

    @Override
    public Widget asWidget() {
        return panel;
    }
}
