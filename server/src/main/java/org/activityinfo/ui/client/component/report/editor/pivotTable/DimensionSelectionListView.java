package org.activityinfo.ui.client.component.report.editor.pivotTable;

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

import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.ListViewDragSource;
import com.extjs.gxt.ui.client.dnd.ListViewDropTarget;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ListView;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.AttributeGroupDTO;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.legacy.shared.reports.model.*;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.component.report.editor.chart.Dimensions;
import org.activityinfo.ui.client.page.report.HasReportElement;
import org.activityinfo.ui.client.page.report.ReportChangeHandler;
import org.activityinfo.ui.client.page.report.ReportEventBus;

import java.util.List;

public class DimensionSelectionListView implements HasReportElement<PivotTableReportElement> {

    public enum Axis {
        ROW,
        COLUMN
    }

    private ReportEventBus reportEventBus;
    private Dispatcher dispatcher;
    private Axis axis;

    private ListStore<DimensionModel> store;
    private ListView<DimensionModel> list;

    private PivotTableReportElement model;

    public DimensionSelectionListView(EventBus eventBus, Dispatcher dispatcher, Axis axis) {
        this.reportEventBus = new ReportEventBus(eventBus, this);
        this.reportEventBus.listen(new ReportChangeHandler() {

            @Override
            public void onChanged() {
                onModelChanged();
            }
        });
        this.dispatcher = dispatcher;
        this.axis = axis;

        store = new ListStore<>();

        list = new ListView<>(store);
        list.setDisplayProperty("name");

        new ListViewDragSource(list);
        ListViewDropTarget target = new ListViewDropTarget(list);
        target.setFeedback(DND.Feedback.INSERT);
        target.setAllowSelfAsSource(true);

        store.addStoreListener(new StoreListener<DimensionModel>() {

            @Override
            public void storeAdd(StoreEvent<DimensionModel> se) {
                updateModelAfterDragDrop();
            }

            @Override
            public void storeRemove(StoreEvent<DimensionModel> se) {
                updateModelAfterDragDrop();
            }

        });
    }

    private void updateModelAfterDragDrop() {
        List<Dimension> dims = Lists.newArrayList();
        for (DimensionModel model : store.getModels()) {
            dims.add(model.getDimension());
        }
        switch (axis) {
            case ROW:
                model.setRowDimensions(dims);
                break;
            case COLUMN:
                model.setColumnDimensions(dims);
        }
        reportEventBus.fireChange();
    }

    @Override
    public void bind(PivotTableReportElement model) {
        this.model = model;
        onModelChanged();
    }

    @Override
    public void disconnect() {
        reportEventBus.disconnect();
    }

    private void onModelChanged() {
        Dimensions.loadDimensions(dispatcher, model).then(new AsyncCallback<Dimensions>() {

            @Override
            public void onFailure(Throwable caught) {
                Log.error(caught.getMessage(), caught);
            }

            @Override
            public void onSuccess(Dimensions result) {
                updateStoreAfterModelChanged(result);
            }
        });
    }

    private List<Dimension> getSelection() {
        switch (axis) {
            case ROW:
                return model.getRowDimensions();
            case COLUMN:
                return model.getColumnDimensions();
        }
        throw new IllegalStateException("" + axis);
    }

    private void updateStoreAfterModelChanged(Dimensions schema) {
        store.setFiresEvents(false);
        store.removeAll();
        for (Dimension dim : getSelection()) {
            DimensionModel model = toModel(dim, schema);
            if (model != null) {
                store.add(model);
            }
        }
        store.setFiresEvents(true);
        list.refresh();
    }

    private DimensionModel toModel(Dimension dim, Dimensions schema) {
        if (dim instanceof DateDimension) {
            return new DimensionModel(((DateDimension) dim).getUnit());
        } else if (dim instanceof AdminDimension) {
            return new DimensionModel(schema.getAdminLevelById(((AdminDimension) dim).getLevelId()));
        } else if (dim instanceof AttributeGroupDimension) {
            AttributeGroupDTO group = schema.getAttributeGroupById(((AttributeGroupDimension) dim)
                    .getAttributeGroupId());
            return group == null ? null : new DimensionModel(group);
        } else {
            switch (dim.getType()) {
                case Database:
                    return new DimensionModel(dim, I18N.CONSTANTS.database());
                case Activity:
                    return new DimensionModel(dim, I18N.CONSTANTS.activity());
                case Indicator:
                    return new DimensionModel(dim, I18N.CONSTANTS.indicator());
                case Partner:
                    return new DimensionModel(dim, I18N.CONSTANTS.partner());
                case Project:
                    return new DimensionModel(dim, I18N.CONSTANTS.project());
                case Location:
                    return new DimensionModel(dim, I18N.CONSTANTS.location());
                case Target:
                    return new DimensionModel(dim, I18N.CONSTANTS.realizedOrTargeted());
                case ActivityCategory:
                    return new DimensionModel(dim, I18N.CONSTANTS.formCategory());
            }
        }
        throw new RuntimeException("Dimension is not supported: " + dim);
    }

    @Override
    public PivotTableReportElement getModel() {
        return model;
    }

    public Component asComponent() {
        return list;
    }
}
