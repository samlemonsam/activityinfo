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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid.ClicksToEdit;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.tips.QuickTip;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.GetSites;
import org.activityinfo.legacy.shared.command.result.SiteResult;
import org.activityinfo.legacy.shared.model.SiteDTO;

/**
 * Displays of sites in a "flat" projection with a paging toolbar. Note: do not
 * use this component directly. Use the SiteGridPanel component.
 */
final class FlatSiteGridPanel extends ContentPanel implements SiteGridPanelView {
    private final Dispatcher dispatcher;

    private EditorGrid<SiteDTO> editorGrid;
    private ListStore<SiteDTO> listStore;
    private PagingToolBar pagingToolBar;

    private Filter currentFilter = new Filter();

    @Inject
    public FlatSiteGridPanel(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;

        setHeaderVisible(false);
        setLayout(new FitLayout());

        pagingToolBar = new PagingToolBar(50);
        setBottomComponent(pagingToolBar);
    }

    public void initGrid(Filter filter, ColumnModel columnModel) {

        PagingLoader<PagingLoadResult<SiteDTO>> loader = new BasePagingLoader<PagingLoadResult<SiteDTO>>(new
                SiteProxy());
        loader.addLoadListener(new LoadListener() {

            @Override
            public void loaderLoadException(LoadEvent le) {
                Log.debug("Exception thrown during load of FlatSiteGrid: ", le.exception);
            }

        });
        loader.setRemoteSort(true);
        loader.setSortField("date2");
        loader.setSortDir(SortDir.DESC);
        pagingToolBar.bind(loader);

        listStore = new ListStore<>(loader);

        if (editorGrid == null) {
            editorGrid = new EditorGrid<SiteDTO>(listStore, columnModel);
            editorGrid.setLoadMask(true);
            // editorGrid.setStateful(true);
            editorGrid.setClicksToEdit(ClicksToEdit.TWO);
            editorGrid.setStripeRows(true);

            final GridSelectionModel<SiteDTO> sm = new GridSelectionModel<SiteDTO>();
            sm.setSelectionMode(SelectionMode.SINGLE);
            sm.addSelectionChangedListener(new SelectionChangedListener<SiteDTO>() {

                @Override
                public void selectionChanged(SelectionChangedEvent<SiteDTO> se) {
                    fireEvent(Events.SelectionChange, se);
                }
            });
            editorGrid.setSelectionModel(sm);

            new QuickTip(editorGrid);
            
            editorGrid.addListener(Events.RowDoubleClick, new Listener<GridEvent>() {

                @Override
                public void handleEvent(GridEvent be) {
                    SiteDTO site = listStore.getAt(be.getRowIndex());
                    SelectionChangedEvent<SiteDTO> event = new SelectionChangedEvent<>(sm, site);
                    fireEvent(Events.RowDoubleClick, event);
                }
            });
            

            add(editorGrid, new BorderLayoutData(Style.LayoutRegion.CENTER));
            layout();

        } else {
            editorGrid.reconfigure(listStore, columnModel);
        }

        this.currentFilter = filter;

        loader.load();
        new QuickTip(editorGrid);
    }

    @Override
    public void addSelectionChangeListener(SelectionChangedListener<SiteDTO> listener) {
        addListener(Events.SelectionChange, listener);
    }

    @Override
    public void addDoubleClickListener(SelectionChangedListener<SiteDTO> listener) {
        addListener(Events.RowDoubleClick, listener);
    }

    private class SiteProxy extends RpcProxy<PagingLoadResult<SiteDTO>> {

        @Override
        protected void load(Object loadConfig, final AsyncCallback<PagingLoadResult<SiteDTO>> callback) {

            PagingLoadConfig config = (PagingLoadConfig) loadConfig;
            GetSites command = new GetSites();
            command.setOffset(config.getOffset());
            command.setLimit(config.getLimit());
            command.setFilter(currentFilter);
            command.setSortInfo(config.getSortInfo());
            dispatcher.execute(command, new AsyncCallback<SiteResult>() {

                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }

                @Override
                public void onSuccess(SiteResult result) {
                    callback.onSuccess(result);
                }
            });
        }
    }

    @Override
    public void refresh() {
        listStore.getLoader().load();
    }

    @Override
    public Component asComponent() {
        return this;
    }

    @Override
    public SiteDTO getSelection() {
        return editorGrid.getSelectionModel().getSelectedItem();
    }
}
