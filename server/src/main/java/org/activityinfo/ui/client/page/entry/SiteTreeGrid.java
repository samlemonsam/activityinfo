package org.activityinfo.ui.client.page.entry;

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

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid.ClicksToEdit;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.tips.QuickTip;
import com.extjs.gxt.ui.client.widget.treegrid.EditorTreeGrid;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.ui.client.page.entry.grouping.AdminGroupingModel;
import org.activityinfo.ui.client.page.entry.grouping.GroupingModel;
import org.activityinfo.ui.client.page.entry.grouping.TimeGroupingModel;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

final class SiteTreeGrid extends EditorTreeGrid<ModelData> implements SiteGridPanelView {

    public static final String ADMIN_STATE_ID = "sitetreegrid.admin";

    public SiteTreeGrid(Dispatcher dispatcher, GroupingModel groupingModel, Filter filter, ColumnModel columnModel) {
        super(createStore(dispatcher, groupingModel), columnModel);
        setLoadMask(true);
        setStateful(true);
        setStateId(ADMIN_STATE_ID);
        setClicksToEdit(ClicksToEdit.TWO);

        setIconProvider(new ModelIconProvider<ModelData>() {
            @Override
            public AbstractImagePrototype getIcon(ModelData model) {
                if (model instanceof SiteDTO) {
                    SiteDTO site = (SiteDTO) model;
                    if (site.hasCoords()) {
                        return IconImageBundle.ICONS.mapped();
                    } else {
                        return IconImageBundle.ICONS.unmapped();
                    }
                } else {
                    return IconImageBundle.ICONS.folder();
                }
            }
        });

        GridSelectionModel<ModelData> sm = new GridSelectionModel<ModelData>();
        sm.setSelectionMode(SelectionMode.SINGLE);
        sm.addSelectionChangedListener(new SelectionChangedListener<ModelData>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<ModelData> se) {
                if (se.getSelectedItem() instanceof SiteDTO) {
                    fireEvent(Events.SelectionChange, se);
                }
            }
        });
        setSelectionModel(sm);

        getLoader().setFilter(filter);
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                new QuickTip(SiteTreeGrid.this);
            }
        });
    }

    private static TreeStore<ModelData> createStore(Dispatcher dispatcher, GroupingModel groupingModel) {

        SiteTreeLoader loader;

        if (groupingModel instanceof AdminGroupingModel) {
            loader = new SiteAdminTreeLoader(dispatcher, (AdminGroupingModel) groupingModel);
        } else if (groupingModel instanceof TimeGroupingModel) {
            loader = new SiteTimeTreeLoader(dispatcher);
        } else {
            throw new IllegalArgumentException("Invalid grouping model " + groupingModel);
        }

        TreeStore<ModelData> treeStore = new TreeStore<ModelData>(loader);
        treeStore.setKeyProvider(loader);
        return treeStore;
    }

    private SiteTreeLoader getLoader() {
        return (SiteTreeLoader) getTreeStore().getLoader();
    }

    @Override
    public void addSelectionChangeListener(SelectionChangedListener<SiteDTO> selectionChangedListener) {
        addListener(Events.SelectionChange, selectionChangedListener);
    }

    @Override
    public void addDoubleClickListener(SelectionChangedListener<SiteDTO> listener) {
        // NOOP
    }

    @Override
    public void refresh() {
        getTreeStore().removeAll();
        getLoader().load();
    }

    @Override
    public Component asComponent() {
        return this;
    }

    @Override
    public SiteDTO getSelection() {
        ModelData selection = getSelectionModel().getSelectedItem();
        if (selection instanceof SiteDTO) {
            return (SiteDTO) selection;
        } else {
            return null;
        }
    }
}
