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
package org.activityinfo.ui.client.component.report.view;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.reports.content.EntityCategory;
import org.activityinfo.legacy.shared.reports.content.PivotTableData;
import org.activityinfo.legacy.shared.reports.model.PivotReportElement;
import org.activityinfo.legacy.shared.reports.util.DateUtil;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.dispatch.type.DateUtilGWTImpl;
import org.activityinfo.ui.client.dispatch.type.IndicatorNumberFormat;
import org.activityinfo.ui.client.page.common.Shutdownable;

import java.util.ArrayList;
import java.util.List;

import static org.activityinfo.legacy.shared.reports.model.DateRange.intersection;

public class DrillDownEditor implements Shutdownable {

    private static final DateUtil DATES = new DateUtilGWTImpl();

    public static final int WIDTH = 600;
    public static final int HEIGHT = 500;

    private Dispatcher dispatcher;
    private Dialog dialog;
    private DrillDownProxy proxy;
    private Grid<DrillDownRow> grid;
    private ListStore<DrillDownRow> store;

    // position
    private int left;
    private int top;


    public DrillDownEditor(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    private void show() {
        if (dialog == null) {
            createDialog();
        }
        store.removeAll();
        if (left > 0 && top > 0) {
            dialog.setPosition(left, top);
        }
        dialog.show();
    }

    @Override
    public void shutdown() {
        dialog.hide();
    }

    public void drillDown(PivotReportElement element, PivotTableData.Axis row, PivotTableData.Axis column) {
        // construct our filter from the intersection of rows and columns
        Filter filter = new Filter(filterFromAxis(row), filterFromAxis(column));

        // apply the effective filter
        final Filter effectiveFilter = new Filter(filter, element.getContent().getEffectiveFilter());

        drillDown(effectiveFilter);
    }

    public void drillDown(Filter effectiveFilter) {
        show();

        // now query the rows:
        proxy.setFilter(effectiveFilter);
        store.getLoader().load();
    }

    private Filter filterFromAxis(PivotTableData.Axis axis) {

        Filter filter = new Filter();
        while (axis != null) {
            if (axis.getDimension() != null) {
                if (axis.getDimension().getType() == DimensionType.Date) {
                    filter.setEndDateRange(intersection(filter.getEndDateRange(),
                            DATES.rangeFromCategory(axis.getCategory())));

                } else if (axis.getCategory() instanceof EntityCategory) {
                    filter.addRestriction(axis.getDimension().getType(), ((EntityCategory) axis.getCategory()).getId());
                }
            }
            axis = axis.getParent();
        }
        return filter;
    }

    private void createDialog() {

        proxy = new DrillDownProxy(dispatcher);
        store = new ListStore<>(new BaseListLoader(proxy));
        grid = new Grid<>(store, buildColumnModel());
        grid.setLoadMask(true);

        dialog = new Dialog();
        dialog.setHeadingText(I18N.CONSTANTS.sites());
        dialog.setButtons(Dialog.CLOSE);
        dialog.setLayout(new FitLayout());
        dialog.setSize(WIDTH, HEIGHT);
        dialog.add(grid);

        dialog.addListener(Events.Move, new Listener<BaseEvent>() {
            @Override
            public void handleEvent(BaseEvent baseEvent) {
                left = -1;
                top = -1;
            }
        });
    }

    private ColumnModel buildColumnModel() {
        List<ColumnConfig> config = new ArrayList<>();
        config.add(new ColumnConfig("database", I18N.CONSTANTS.database(), 100));
        config.add(new ColumnConfig("activity", I18N.CONSTANTS.activity(), 100));
        config.add(new ColumnConfig("partner", I18N.CONSTANTS.partner(), 100));
        config.add(new ColumnConfig("location", I18N.CONSTANTS.location(), 100));
        config.add(new ColumnConfig("date", I18N.CONSTANTS.date(), 100));
        config.add(new ColumnConfig("indicator", I18N.CONSTANTS.indicator(), 100));
        
        ColumnConfig valueColumn = new ColumnConfig("value", I18N.CONSTANTS.value(), 100);
        valueColumn.setNumberFormat(IndicatorNumberFormat.INSTANCE);
        valueColumn.setAlignment(Style.HorizontalAlignment.RIGHT);
        config.add(valueColumn);

        return new ColumnModel(config);
    }

    public void setPosition(int left, int top) {
        this.left = left;
        this.top = top;
    }
}
