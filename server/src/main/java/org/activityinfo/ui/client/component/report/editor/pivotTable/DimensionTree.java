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

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TreePanelEvent;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.client.monitor.MaskingAsyncMonitor;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.command.result.AdminLevelResult;
import org.activityinfo.legacy.shared.command.result.AttributeGroupResult;
import org.activityinfo.legacy.shared.model.AdminLevelDTO;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.legacy.shared.reports.model.*;
import org.activityinfo.promise.Promise;
import org.activityinfo.server.database.hibernate.entity.Attribute;
import org.activityinfo.server.database.hibernate.entity.AttributeGroup;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.component.report.editor.chart.Dimensions;
import org.activityinfo.ui.client.page.report.HasReportElement;
import org.activityinfo.ui.client.page.report.ReportChangeHandler;
import org.activityinfo.ui.client.page.report.ReportEventBus;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Maintains a list of dimensions applicable for the current element
 */
public class DimensionTree implements HasReportElement<PivotTableReportElement> {

    private final Dispatcher dispatcher;
    private final ReportEventBus reportEventBus;
    private boolean fireEvents = true;

    private final TreeStore<DimensionModel> store;
    private final TreePanel<DimensionModel> treePanel;

    private Set<Integer> previouslyLoaded = Collections.emptySet();

    private PivotTableReportElement model;
    private DimensionModel geographyRoot;
    private final List<DimensionModel> attributeDimensions = Lists.newArrayList();

    public DimensionTree(EventBus eventBus, Dispatcher dispatcher) {
        this.reportEventBus = new ReportEventBus(eventBus, this);
        this.reportEventBus.listen(new ReportChangeHandler() {
            @Override
            public void onChanged() {
                if (fireEvents) {
                    fireEvents = false;
                    onModelChanged();
                    fireEvents = true;
                }
            }
        });
        this.dispatcher = dispatcher;

        this.store = new TreeStore<DimensionModel>();
        addDimension(DimensionType.Database, I18N.CONSTANTS.database());
        addDimension(DimensionType.Activity, I18N.CONSTANTS.activity());
        addDimension(DimensionType.ActivityCategory, I18N.CONSTANTS.formCategory());
        addDimension(DimensionType.Indicator, I18N.CONSTANTS.indicator());
        addDimension(DimensionType.Partner, I18N.CONSTANTS.partner());
        addDimension(DimensionType.Project, I18N.CONSTANTS.project());
        addDimension(DimensionType.Target, I18N.CONSTANTS.realizedOrTargeted());
        addTimeDimensions();
        addGeographyRoot();

        treePanel = new TreePanel<DimensionModel>(store);
        treePanel.setBorders(true);
        treePanel.setCheckable(true);
        treePanel.setCheckNodes(TreePanel.CheckNodes.LEAF);
        treePanel.setCheckStyle(TreePanel.CheckCascade.NONE);
        treePanel.getStyle().setNodeCloseIcon(null);
        treePanel.getStyle().setNodeOpenIcon(null);
        treePanel.setStateful(true);
        treePanel.setStateId("dimensionTree");
        treePanel.setDisplayProperty("name");
        treePanel.addListener(Events.Expand, new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {
                applyModelState();
            }
        });

        /* enable drag and drop for dev */
        // TreePanelDragSource source = new TreePanelDragSource(treePanel);
        // source.setTreeSource(DND.TreeSource.LEAF);
        /* end enable drag and drop for dev */

        treePanel.collapseAll();

        treePanel.addListener(Events.CheckChange, new Listener<TreePanelEvent<DimensionModel>>() {

            @Override
            public void handleEvent(TreePanelEvent<DimensionModel> be) {
                updateModelAfterCheckChange(be);
            }
        });
    }

    private void addGeographyRoot() {
        geographyRoot = new DimensionModel(I18N.CONSTANTS.geography());
        store.add(geographyRoot, false);
        addLocationDimension();
    }

    private void addLocationDimension() {
        store.add(geographyRoot, new DimensionModel(DimensionType.Location, I18N.CONSTANTS.location()), false);
    }

    private void addDimension(DimensionType type, String name) {
        store.add(new DimensionModel(type, name), false);
    }

    private void addTimeDimensions() {
        DimensionModel folder = new DimensionModel(I18N.CONSTANTS.time());
        store.add(folder, false);
        store.add(folder, new DimensionModel(DateUnit.YEAR), false);
        store.add(folder, new DimensionModel(DateUnit.QUARTER), false);
        store.add(folder, new DimensionModel(DateUnit.MONTH), false);
        store.add(folder, new DimensionModel(DateUnit.WEEK_MON), false);

    }

    @Override
    public void bind(PivotTableReportElement model) {
        this.model = model;
        applyModelState();
    }

    @Override
    public PivotTableReportElement getModel() {
        return model;
    }

    private void onModelChanged() {
        if (needToReloadDimensions(model)) {
            clearIndicatorSpecificDimensions();
            Dimensions.loadDimensions(dispatcher, model).then(new AsyncCallback<Dimensions>() {
                @Override
                public void onFailure(Throwable caught) {

                }

                @Override
                public void onSuccess(Dimensions result) {
                    populateIndicatorSpecificDimensions(result);
                    applyModelState(result);
                }
            });
        }
    }


    private void applyModelState() {
        Dimensions.loadDimensions(dispatcher, model).then(new AsyncCallback<Dimensions>() {

            @Override
            public void onFailure(Throwable caught) {

            }

            @Override
            public void onSuccess(Dimensions result) {
                applyModelState(result);
            }
        });
    }


    private void applyModelState(Dimensions schema) {
        for (DimensionModel node : store.getAllItems()) {
            if (node.hasDimension()) {
                treePanel.setChecked(node, isDimensionSelected(schema, node.getDimension()));
            }
        }
    }

    private boolean isDimensionSelected(Dimensions schema, Dimension dim) {
        return isDimensionSelected(schema, model.getRowDimensions(), dim) ||
               isDimensionSelected(schema, model.getColumnDimensions(), dim);
    }

    private boolean isDimensionSelected(Dimensions schema, List<Dimension> dims, Dimension dim) {

        if (dim instanceof AttributeGroupDimension) {
            return isAttributeDimensionSelected(schema, dims, (AttributeGroupDimension) dim);
        } else {
            return dims.contains(dim);
        }
    }

    private boolean isAttributeDimensionSelected(Dimensions schema, List<Dimension> dims, AttributeGroupDimension dim) {

        String name = schema.getAttributeGroupNameSafe(dim.getAttributeGroupId());

        for (Dimension selectedDim : dims) {
            if (selectedDim instanceof AttributeGroupDimension) {
                int selectedId = ((AttributeGroupDimension) selectedDim).getAttributeGroupId();
                String selectedName = schema.getAttributeGroupNameSafe(selectedId);
                if (selectedName.equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;

    }

    private boolean needToReloadDimensions(PivotTableReportElement model) {
        return !previouslyLoaded.containsAll(model.getIndicators()) ||
               !model.getIndicators().containsAll(previouslyLoaded);
    }

    private void clearIndicatorSpecificDimensions() {

        for (DimensionModel model : Lists.newArrayList(store.getAllItems())) {
            if (model.hasDimension() && (model.getDimension() instanceof AttributeGroupDimension ||
                                         model.getDimension() instanceof AdminDimension)) {
                Log.info("Removing " + model.getCaption());
                try {
                    store.remove(model);
                } catch (Exception e) {
                    // workaround for buggy GXT
                    Log.debug("Exception thrown removing " + model.getCaption(), e);
                }
            }
        }
    }

    private void populateIndicatorSpecificDimensions(Dimensions schema) {
        addGeography(schema.getAdminLevelDimensions());
        addAttributeGroups(schema);
        previouslyLoaded = model.getIndicators();
    }

    private void addGeography(List<DimensionModel> levels) {

        store.removeAll(geographyRoot);
        for (DimensionModel level : levels) {
            store.add(geographyRoot, level, false);
        }
        addLocationDimension();
    }

    private void addAttributeGroups(Dimensions groups) {

        // clear existing attributes
        for (DimensionModel model : attributeDimensions) {
            store.remove(model);
        }
        attributeDimensions.clear();

        List<DimensionModel> models = groups.getAttributeDimensions();
        store.add(models, false);
        attributeDimensions.addAll(models);
    }


    private void updateModelAfterCheckChange(TreePanelEvent<DimensionModel> event) {
        Dimension dim = event.getItem().getDimension();

        if (event.isChecked()) {
            if (!model.getRowDimensions().contains(dim) && !model.getColumnDimensions().contains(dim)) {

                if (model.getRowDimensions().size() > model.getColumnDimensions().size()) {
                    model.addColDimension(dim);
                } else {
                    model.addRowDimension(dim);
                }
            }
        } else {
            model.getRowDimensions().remove(dim);
            model.getColumnDimensions().remove(dim);
        }

        if (fireEvents) {
            reportEventBus.fireChange();
        }
    }

    public Component asComponent() {
        return treePanel;
    }

    @Override
    public void disconnect() {
        reportEventBus.disconnect();
    }
}
