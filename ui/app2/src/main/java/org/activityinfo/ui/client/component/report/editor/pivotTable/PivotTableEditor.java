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
package org.activityinfo.ui.client.component.report.editor.pivotTable;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.inject.Inject;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.RenderElement.Format;
import org.activityinfo.legacy.shared.reports.model.PivotTableReportElement;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.component.report.editor.chart.PivotFilterPanel;
import org.activityinfo.ui.client.component.report.view.PivotGridPanel;
import org.activityinfo.ui.client.dispatch.AsyncMonitor;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.dispatch.ResourceLocator;
import org.activityinfo.ui.client.dispatch.monitor.MaskingAsyncMonitor;
import org.activityinfo.ui.client.dispatch.state.StateProvider;
import org.activityinfo.ui.client.page.report.editor.ReportElementEditor;

import java.util.Arrays;
import java.util.List;

public class PivotTableEditor extends LayoutContainer implements ReportElementEditor<PivotTableReportElement> {

    private final EventBus eventBus;
    private final Dispatcher service;
    private ResourceLocator locator;
    private final StateProvider stateMgr;

    private PivotTrayPanel pivotPanel;
    private PivotFilterPanel filterPane;
    private PivotTableBinder viewBinder;

    private DimensionPruner pruner;

    private LayoutContainer center;
    private PivotGridPanel gridPanel;

    private PivotTableReportElement model;

    @Inject
    public PivotTableEditor(EventBus eventBus, Dispatcher service, ResourceLocator locator, StateProvider stateMgr) {
        this.eventBus = eventBus;
        this.service = service;
        this.locator = locator;
        this.stateMgr = stateMgr;

        initializeComponent();

        createPane();
        createFilterPane();
        createGridContainer();

        this.pruner = new DimensionPruner(eventBus, service);

    }

    private void initializeComponent() {
        BorderLayout borderLayout = new BorderLayout();
        borderLayout.setEnableState(true);
        setStateId("pivotPage");
        setLayout(borderLayout);
    }

    private void createPane() {

        pivotPanel = new PivotTrayPanel(eventBus, service);

        BorderLayoutData east = new BorderLayoutData(Style.LayoutRegion.EAST);
        east.setCollapsible(true);
        east.setSplit(true);
        east.setMargins(new Margins(0, 5, 0, 0));

        add(pivotPanel, east);
    }

    private void createFilterPane() {
        filterPane = new PivotFilterPanel(eventBus, service, locator);
        filterPane.applyBaseFilter(new Filter());

        BorderLayoutData west = new BorderLayoutData(Style.LayoutRegion.WEST);
        west.setMinSize(250);
        west.setSize(250);
        west.setCollapsible(true);
        west.setSplit(true);
        west.setMargins(new Margins(0, 0, 0, 0));
        add(filterPane, west);
    }

    private void createGridContainer() {
        center = new LayoutContainer();
        center.setLayout(new BorderLayout());
        add(center, new BorderLayoutData(Style.LayoutRegion.CENTER));

        gridPanel = new PivotGridPanel(service);
        gridPanel.setHeaderVisible(true);
        gridPanel.setHeadingText(I18N.CONSTANTS.preview());

        center.add(gridPanel, new BorderLayoutData(Style.LayoutRegion.CENTER));

        viewBinder = new PivotTableBinder(eventBus, service, gridPanel);
    }

    public AsyncMonitor getMonitor() {
        return new MaskingAsyncMonitor(this, I18N.CONSTANTS.loading());
    }

    @Override
    public PivotTableReportElement getModel() {
        return model;
    }

    @Override
    public void bind(PivotTableReportElement model) {
        this.model = model;
        pivotPanel.bind(model);
        filterPane.bind(model);
        viewBinder.bind(model);
        pruner.bind(model);
    }

    @Override
    public void disconnect() {
        pivotPanel.disconnect();
        filterPane.disconnect();
        viewBinder.disconnect();
        pruner.disconnect();
    }

    @Override
    public Component getWidget() {
        return this;
    }

    @Override
    public List<Format> getExportFormats() {
        return Arrays.asList(Format.Excel, Format.Word, Format.PDF);
    }

}
