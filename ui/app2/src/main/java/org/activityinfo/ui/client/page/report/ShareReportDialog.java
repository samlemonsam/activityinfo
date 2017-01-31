package org.activityinfo.ui.client.page.report;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.command.result.BatchResult;
import org.activityinfo.legacy.shared.command.result.IndicatorResult;
import org.activityinfo.legacy.shared.command.result.ReportVisibilityResult;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.model.IndicatorDTO;
import org.activityinfo.legacy.shared.model.ReportDTO;
import org.activityinfo.legacy.shared.model.ReportMetadataDTO;
import org.activityinfo.legacy.shared.model.ReportVisibilityDTO;
import org.activityinfo.legacy.shared.reports.model.Report;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.dispatch.callback.SuccessCallback;
import org.activityinfo.ui.client.dispatch.monitor.MaskingAsyncMonitor;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ShareReportDialog extends Dialog {

    private final Dispatcher dispatcher;
    private final ListStore<ReportVisibilityDTO> gridStore;
    private CheckColumnConfig visibleColumn;
    private CheckColumnConfig dashboardColumn;
    private Report currentReport;
    private final Grid<ReportVisibilityDTO> grid;

    public ShareReportDialog(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;

        setHeadingText(I18N.CONSTANTS.shareReport());
        setWidth(450);
        setHeight(350);
        setButtons(Dialog.OKCANCEL);

        gridStore = new ListStore<ReportVisibilityDTO>();
        grid = new Grid<ReportVisibilityDTO>(gridStore, createColumnModel());
        grid.addPlugin(visibleColumn);
        grid.addPlugin(dashboardColumn);
        add(grid);

        setLayout(new FitLayout());

    }

    private ColumnModel createColumnModel() {

        ColumnConfig icon = new ColumnConfig("icon", "", 26);
        icon.setSortable(false);
        icon.setResizable(false);
        icon.setMenuDisabled(true);
        icon.setRenderer(new GridCellRenderer<ReportVisibilityDTO>() {

            @Override
            public SafeHtml render(ReportVisibilityDTO model,
                                   String property,
                                   ColumnData config,
                                   int rowIndex,
                                   int colIndex,
                                   ListStore<ReportVisibilityDTO> store,
                                   Grid<ReportVisibilityDTO> grid) {
                return IconImageBundle.ICONS.group().getSafeHtml();
            }
        });

        ColumnConfig name = new ColumnConfig("databaseName", I18N.CONSTANTS.group(), 150);
        name.setRenderer(new GridCellRenderer<ReportVisibilityDTO>() {

            @Override
            public SafeHtml render(ReportVisibilityDTO model,
                                 String property,
                                 ColumnData config,
                                 int rowIndex,
                                 int colIndex,
                                 ListStore<ReportVisibilityDTO> store,
                                 Grid<ReportVisibilityDTO> grid) {

                return SafeHtmlUtils.fromString(I18N.MESSAGES.databaseUserGroup(model.getDatabaseName()));

            }
        });

        visibleColumn = new CheckColumnConfig("visible", I18N.CONSTANTS.shared(), 75);
        visibleColumn.setDataIndex("visible");

        dashboardColumn = new CheckColumnConfig("defaultDashboard", I18N.CONSTANTS.defaultDashboard(), 75) {
            @Override
            protected void onMouseDown(GridEvent<ModelData> ge) {
                super.onMouseDown(ge);
                Record record = grid.getStore().getRecord(ge.getModel());
    
                // Putting on the dashboard implies sharing
                if(record.get("defaultDashboard") == Boolean.TRUE &&
                   record.get("visible") != Boolean.TRUE) {
                    
                    record.set("visible", true);
                }
            }
        };
        dashboardColumn.setDataIndex("defaultDashboard");

        return new ColumnModel(Arrays.asList(icon, name, visibleColumn, dashboardColumn));
    }

    public void show(ReportMetadataDTO metadata) {
        super.show();

        dispatcher.execute(new GetReportModel(metadata.getId()), new MaskingAsyncMonitor(grid, I18N.CONSTANTS.loading()),
                new SuccessCallback<ReportDTO>() {

                    @Override
                    public void onSuccess(ReportDTO reportDTO) {
                        show(reportDTO.getReport());
                    }
                });
    }

    public void show(final Report report) {
        if (report.getIndicators().isEmpty()) {
            MessageBox.alert(I18N.CONSTANTS.alert(), I18N.CONSTANTS.unableToShareReport(), null);
            return;
        }

        super.show();

        this.currentReport = report;

        // we need to combine the databases which already have visiblity with
        // those that could potentially be added
        BatchCommand batch = new BatchCommand();
        batch.add(new GetIndicators(currentReport.getIndicators()));
        batch.add(new GetReportVisibility(currentReport.getId()));

        dispatcher.execute(batch,
                new MaskingAsyncMonitor(grid, I18N.CONSTANTS.loading()),
                new SuccessCallback<BatchResult>() {
                    @Override
                    public void onSuccess(BatchResult batch) {
                        populateGrid((IndicatorResult) batch.getResult(0), (ReportVisibilityResult) batch.getResult(1));
                    }
                });
    }


    private void populateGrid(IndicatorResult indicators, ReportVisibilityResult visibility) {
        gridStore.removeAll();
        Map<Integer, ReportVisibilityDTO> databases = Maps.newHashMap();

        for (ReportVisibilityDTO model : visibility.getList()) {
            databases.put(model.getDatabaseId(), model);
        }

        for (IndicatorDTO indicator : indicators.getIndicators()) {
            if (!databases.containsKey(indicator.getDatabaseId())) {
                ReportVisibilityDTO model = new ReportVisibilityDTO();
                model.setDatabaseId(indicator.getDatabaseId());
                model.setDatabaseName(indicator.getDatabaseName());
                databases.put(indicator.getDatabaseId(), model);
            }
        }

        for (ReportVisibilityDTO model : databases.values()) {
            gridStore.add(model);
        }

        if (gridStore.getCount() == 0) {
            MessageBox.alert(I18N.CONSTANTS.share(), I18N.CONSTANTS.emptyReportsCannotBeShared(),
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        hide();
                    }
                });
        }
    }

    @Override
    protected void onButtonPressed(Button button) {
        if (button.getItemId().equals(Dialog.OK)) {
            save();
        } else {
            hide();
        }
    }

    private void save() {
        List<ReportVisibilityDTO> toSave = Lists.newArrayList();
        for (ReportVisibilityDTO model : gridStore.getModels()) {
            if (gridStore.getRecord(model).isDirty()) {
                toSave.add(model);
            }
        }

        if (toSave.isEmpty()) {
            hide();
        } else {
            dispatcher.execute(new UpdateReportVisibility(currentReport.getId(), toSave),
                    new MaskingAsyncMonitor(grid, I18N.CONSTANTS.saving()),
                    new SuccessCallback<VoidResult>() {
                        @Override
                        public void onSuccess(VoidResult result) {
                            hide();
                        }
                    });
        }
    }

}
