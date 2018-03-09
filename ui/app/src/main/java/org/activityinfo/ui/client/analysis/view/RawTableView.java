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

import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.analysis.viewModel.AnalysisResult;
import org.activityinfo.ui.client.analysis.viewModel.AnalysisViewModel;
import org.activityinfo.ui.client.analysis.viewModel.DimensionSet;
import org.activityinfo.ui.client.analysis.viewModel.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class RawTableView implements IsWidget {

    private static final Logger LOGGER = Logger.getLogger(RawTableView.class.getName());

    private final AnalysisViewModel model;
    private ListStore<Point> store;
    private ContentPanel panel;
    private Grid<Point> grid;

    public RawTableView(AnalysisViewModel model) {
        this.model = model;
        this.store = new ListStore<>(point -> point.toString());
        this.grid = new Grid<>(store, buildColumnModel(new DimensionSet()));
        this.grid.getView().setSortingEnabled(false);
        this.panel = new ContentPanel();
        this.panel.setHeading("Results");
        this.panel.add(grid);

        model.getResultTable().subscribe(observable -> {
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
