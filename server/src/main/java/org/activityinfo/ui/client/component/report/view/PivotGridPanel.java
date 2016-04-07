package org.activityinfo.ui.client.component.report.view;

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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.client.type.IndicatorNumberFormat;
import org.activityinfo.legacy.shared.reports.content.PivotContent;
import org.activityinfo.legacy.shared.reports.content.PivotTableData;
import org.activityinfo.legacy.shared.reports.model.PivotReportElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PivotGridPanel extends ContentPanel implements ReportView<PivotReportElement<PivotContent>> {

    interface Templates extends SafeHtmlTemplates {

        @Template("<span style=\"margin-left:{1}px\">{0}</span>")
        SafeHtml header(String header, int indent);

    }

    private static final Templates TEMPLATES = GWT.create(Templates.class);

    private static final int ROW_INDENT = 15;

    private Grid<PivotTableRow> grid;
    private ListStore<PivotTableRow> store;
    private Map<PivotTableData.Axis, String> propertyMap;
    private Map<Integer, PivotTableData.Axis> columnMap;

    private ColumnModel columnModel;
    private DrillDownEditor drillDownEditor;

    public PivotGridPanel(Dispatcher dispatcher) {
        this.drillDownEditor = new DrillDownEditor(dispatcher);
        setLayout(new FitLayout());
    }

    @SuppressWarnings("GwtInconsistentSerializableClass")
    public class PivotTableRow extends BaseTreeModel {
        private PivotTableData.Axis rowAxis;
        private int depth;

        public PivotTableRow(PivotTableData.Axis axis, int depth) {
            this.rowAxis = axis;
            this.depth = depth;
            set("header", axis.getLabel());

            for (Map.Entry<PivotTableData.Axis, PivotTableData.Cell> entry : axis.getCells().entrySet()) {
                set(propertyMap.get(entry.getKey()), entry.getValue().getValue());
            }
        }

        public PivotTableData.Axis getRowAxis() {
            return rowAxis;
        }

        public int getDepth() {
            return depth;
        }
    }

    @Override
    public void loading() {
        if(isRendered()) {
            el().mask();
        }
    }

    @Override
    public void onFailure(Throwable caught, ClickHandler retryCallback) {
        ReportViewRetrier.onFailure(this, caught, retryCallback);
    }

    @Override
    public void show(final PivotReportElement element) {
        if(isRendered()) {
            el().unmask();
        }

        if (grid != null) {
            removeAll();
        }

        PivotTableData data = element.getContent().getData();

        propertyMap = new HashMap<>();
        columnMap = new HashMap<>();
        columnModel = createColumnModel(data);

        store = new ListStore<>();

        addRows(data.getRootRow(), 0);

        grid = new Grid<>(store, columnModel);
        grid.setAutoExpandColumn("header");
        grid.setAutoExpandMin(150);
        grid.setView(new PivotGridView());
        grid.setSelectionModel(new CellSelectionModel<PivotGridPanel.PivotTableRow>());
        grid.addListener(Events.CellDoubleClick, new Listener<GridEvent<PivotTableRow>>() {
            @Override
            public void handleEvent(GridEvent<PivotTableRow> ge) {
                if (ge.getColIndex() != 0) {
                    PivotTableData.Axis row = ge.getModel().getRowAxis();
                    PivotTableData.Axis column = columnMap.get(ge.getColIndex());
                    if (row.getCell(column) != null) {
                        drillDownEditor.drillDown(element, row, column);
                    }
                }
            }
        });

        add(grid);


        layout();
    }

    private void addRows(PivotTableData.Axis parent, int depth) {
        for (PivotTableData.Axis axis : parent.getChildren()) {
            store.add(new PivotTableRow(axis, depth));
            addRows(axis, depth + 1);
        }
    }

    private static class RowHeaderRenderer implements GridCellRenderer<PivotTableRow> {

        @Override
        public SafeHtml render(PivotTableRow model,
                             String property,
                             ColumnData config,
                             int rowIndex,
                             int colIndex,
                             ListStore<PivotTableRow> store,
                             Grid<PivotTableRow> grid) {
            return TEMPLATES.header((String) model.get("header"), model.getDepth() * ROW_INDENT);
        }

    }

    protected ColumnModel createColumnModel(PivotTableData data) {

        List<ColumnConfig> config = new ArrayList<>();

        ColumnConfig rowHeader = new ColumnConfig("header", "", 150);
        rowHeader.setRenderer(new RowHeaderRenderer());
        rowHeader.setSortable(false);
        rowHeader.setMenuDisabled(true);
        config.add(rowHeader);

        int colIndex = 1;

        List<PivotTableData.Axis> leaves = data.getRootColumn().getLeaves();
        for (PivotTableData.Axis axis : leaves) {

            String id = "col" + colIndex;

            String label = axis.getLabel();
            if (label == null) {
                label = I18N.CONSTANTS.value();
            }
            ColumnConfig column = new ColumnConfig(id, label, 75);

            column.setNumberFormat(IndicatorNumberFormat.INSTANCE);
            column.setAlignment(Style.HorizontalAlignment.RIGHT);
            column.setSortable(false);
            column.setMenuDisabled(true);
            column.setRenderer(new GridCellRenderer() {
                @Override
                public SafeHtml render(ModelData model,
                                       String property,
                                       ColumnData config,
                                       int rowIndex,
                                       int colIndex,
                                       ListStore store,
                                       Grid grid) {

                    Double value = model.get(property);
                    if (value == null) {
                        config.cellAttr = "";
                        return null;
                    } else {
                        config.cellAttr = "data-pivot='value'";
                        return SafeHtmlUtils.fromTrustedString(IndicatorNumberFormat.INSTANCE.format(value));
                    }
                }
            });

            propertyMap.put(axis, id);
            columnMap.put(colIndex, axis);

            config.add(column);
            colIndex++;
        }

        ColumnModel columnModel = new ColumnModel(config);

        int depth = data.getRootColumn().getDepth();
        int row = 0;

        for (int d = 1; d <= depth; ++d) {
            List<PivotTableData.Axis> children = data.getRootColumn().getDescendantsAtDepth(d);

            if (d < depth) {
                int col = 1;
                for (PivotTableData.Axis child : children) {

                    int colSpan = child.getLeaves().size();
                    columnModel.addHeaderGroup(row, col,
                            new HeaderGroupConfig(SafeHtmlUtils.fromString(child.getLabel()), 1, colSpan));

                    col += colSpan;
                }
                row++;
            }
        }
        return columnModel;
    }

    @Override
    public Component asComponent() {
        return this;
    }
}
