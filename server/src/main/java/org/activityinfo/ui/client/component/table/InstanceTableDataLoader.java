package org.activityinfo.ui.client.component.table;
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

import com.google.common.collect.Sets;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.ListDataProvider;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.ObservablePromise;
import org.activityinfo.observable.Observer;
import org.activityinfo.ui.client.widget.CellTable;
import org.activityinfo.ui.client.widget.loading.LoadingState;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author yuriyz on 4/10/14.
 */
public class InstanceTableDataLoader {

    private static final int PAGE_SIZE = 200;
    private static final double LOAD_THRESHOLD_FRACTION = 0.65;

    private static final Logger LOGGER = Logger.getLogger(InstanceTableDataLoader.class.getName());

    private final ListDataProvider<RowView> tableDataProvider = new ListDataProvider<>();
    private final InstanceTable table;
    private final Set<FieldPath> fields = Sets.newHashSet();

    private int lastVerticalScrollPosition;
    private int totalCount = -1;
    private ColumnSet prefetchedResult;

    public InstanceTableDataLoader(InstanceTable table) {
        this.table = table;
        tableDataProvider.addDataDisplay(table.getTable());

        // infinite scroll loading
        table.getTable().getEventBus().addHandler(CellTable.ScrollEvent.TYPE, new CellTable.ScrollHandler() {
            @Override
            public void onScroll(CellTable.ScrollEvent event) {
                loadOnScroll(event);
            }
        });
    }

    private void loadOnScroll(CellTable.ScrollEvent event) {
        if (table.getLoadingIndicator().isLoading()) {
            return; // skip if load is already in progress
        }

        final int oldScrollPos = lastVerticalScrollPosition;
        lastVerticalScrollPosition = event.getVerticalScrollPosition();

        // If scrolling up, ignore the event.
        if (oldScrollPos >= lastVerticalScrollPosition) {
            return;
        }


        int maxScrollTop = table.getTable().getOffsetHeight()
                - event.getScrollAncestor().getOffsetHeight();

//        GWT.log("scrollPos: " + lastVerticalScrollPosition +
//                        ", maxScrollTop:" + maxScrollTop +
//                        ", tableHeight:" + table.getTable().getOffsetHeight() +
//                        ", scrollHeight: " + event.getScrollAncestor().getOffsetHeight() +
//                        ", threshold: " + maxScrollTop * LOAD_THRESHOLD_FRACTION
//        );

        // if near the end then load data
        if (lastVerticalScrollPosition >= (maxScrollTop * LOAD_THRESHOLD_FRACTION)) {
            loadMore();
        }
    }

    public void loadMore() {
        loadMore(false);
    }

    public void loadMore(boolean isPrefetchCall) {
        if (!isAllLoaded()) {
            int offset = offset();
            final int countToLoad = Math.min(PAGE_SIZE, totalCount - offset);
            load(offset, countToLoad, isPrefetchCall);
        }
    }

    public boolean isAllLoaded() {
        return offset() >= totalCount && totalCount != -1;
    }

    private int offset() {
        return tableDataProvider.getList().size();
    }

    /**
     * Loads data and append to table.
     *
     * @param offset      offset
     * @param countToLoad count
     */
    private void load(final int offset, int countToLoad, final boolean isPrefetchCall) {

        if (!isPrefetchCall && prefetchedResult != null) {
            LOGGER.log(Level.FINE, "Pre-fetched instances applied. Pre-fetch again.");

            applyQueryResult(prefetchedResult);
            prefetchedResult = null;

            prefetch();
            return;
        }

        if (table.getLoadingIndicator().isLoading()) {
            LOGGER.log(Level.FINE, "Loading already in progress. Skip!");
            return;
        }

        if (isAllLoaded()) {
            LOGGER.log(Level.FINE, "All data are already loaded.");
            return;
        }

        LOGGER.log(Level.FINE, "Loading instances... offset = " +
                offset + ", count = " + countToLoad + ", totalCount = " + totalCount + ", fields = " + fields);

        table.getLoadingIndicator().onLoadingStateChanged(LoadingState.LOADING, I18N.CONSTANTS.loading());

        QueryModel queryModel = newQueryModel();
        queryModel.setFilter(table.getFilter());

        for (FieldColumn column : table.getColumns()) {
            queryModel.selectField(column.getNode().getFieldId()).as(column.getNode().getFieldId().asString());
        }

        Observable<ColumnSet> observable = table.getResourceLocator().queryTable(queryModel);
        observable.subscribe(new Observer<ColumnSet>() {
            @Override
            public void onChange(Observable<ColumnSet> observable) {
                if (!observable.isLoading()) {
                    table.getLoadingIndicator().onLoadingStateChanged(LoadingState.LOADED);

                    if (isPrefetchCall) { // just save prefetch and exit
                        prefetchedResult = observable.get();
                        return;
                    }

                    ColumnSet columnSet = observable.get();
                    if (columnSet != null) {
                        applyQueryResult(columnSet);

                        prefetch();
                    }
                }

            }
        });
        if (observable instanceof ObservablePromise) {
            ((ObservablePromise)observable).getPromise().then(new AsyncCallback() {
                @Override
                public void onFailure(Throwable caught) {
                    LOGGER.log(Level.SEVERE, "Failed to load instances. fields = " + fields, caught);
                    table.getLoadingIndicator().onLoadingStateChanged(LoadingState.FAILED, caught);
                }

                @Override
                public void onSuccess(Object result) {
                }
            });
        }
    }

    public QueryModel newQueryModel() {
        QueryModel queryModel = new QueryModel(table.getRootFormClass().getId());
        queryModel.selectResourceId().as("@id");
        return queryModel;
    }

    private void applyQueryResult(ColumnSet columnSet) {
        LOGGER.log(Level.FINE, "Loaded column set = " + columnSet);

        tableDataProvider.getList().addAll(RowView.asRowViews(columnSet));

        InstanceTableDataLoader.this.totalCount = columnSet.getNumRows();

        refreshTableActionsState();
    }

    private void refreshTableActionsState() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                table.getTable().redrawHeaders();
            }
        });
    }

    private void prefetch() {
        loadMore(true);
    }

    public void reload() {
        prefetchedResult = null;
        totalCount = -1;
        tableDataProvider.getList().clear();

        load(0, PAGE_SIZE, false);
    }

    public Set<FieldPath> getFields() {
        return fields;
    }

    public void reset() {
        fields.clear();
        prefetchedResult = null;
    }
}
