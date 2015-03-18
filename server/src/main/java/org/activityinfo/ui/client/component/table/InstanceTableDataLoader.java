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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.ListDataProvider;
import org.activityinfo.core.client.InstanceQuery;
import org.activityinfo.core.client.QueryResult;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.widget.CellTable;
import org.activityinfo.ui.client.widget.loading.LoadingState;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author yuriyz on 4/10/14.
 */
public class InstanceTableDataLoader {

    private static final int PAGE_SIZE = 100;

    public static interface DataLoadHandler extends EventHandler {

        void onLoad(DataLoadEvent event);
    }

    public static class DataLoadEvent extends GwtEvent<DataLoadHandler> {

        public static final Type<DataLoadHandler> TYPE = new Type<>();

        private final int totalCount;
        private final int loadedDataCount;
        private boolean failed = false;

        public DataLoadEvent(int totalCount, int loadedDataCount) {
            this.totalCount = totalCount;
            this.loadedDataCount = loadedDataCount;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public int getLoadedDataCount() {
            return loadedDataCount;
        }

        public boolean isFailed() {
            return failed;
        }

        public void setFailed(boolean failed) {
            this.failed = failed;
        }

        @Override
        public Type<DataLoadHandler> getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(DataLoadHandler handler) {
            handler.onLoad(this);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(InstanceTableDataLoader.class.getName());

    private final ListDataProvider<Projection> tableDataProvider = new ListDataProvider<>();
    private final InstanceTable table;
    private final Set<FieldPath> fields = Sets.newHashSet();

    private int lastVerticalScrollPosition;
    private int instanceTotalCount = -1;

    public InstanceTableDataLoader(InstanceTable table) {
        this.table = table;
        tableDataProvider.addDataDisplay(table.getTable());
        table.getTable().getEventBus().addHandler(CellTable.ScrollEvent.TYPE, new CellTable.ScrollHandler() {
            @Override
            public void onScroll(CellTable.ScrollEvent event) {
                handleScroll(event);
            }
        });
    }

    private void handleScroll(CellTable.ScrollEvent event) {
        final int oldScrollPos = lastVerticalScrollPosition;
        lastVerticalScrollPosition = event.getVerticalScrollPosition();

        // If scrolling up, ignore the event.
        if (oldScrollPos >= lastVerticalScrollPosition) {
            return;
        }

        int maxScrollTop = table.getTable().getOffsetHeight()
                - event.getScrollAncestor().getOffsetHeight();

        // if near the end then load data
        if (lastVerticalScrollPosition >= maxScrollTop) {
            // AI-524: as pointed in issue for now we will put "Load more" button instead of
            // loading data on scrolling
            // loadMore();
        }
    }

    public void loadMore() {
        final int offset = tableDataProvider.getList().size();
        // load data only if offset is less then total count (and totalCount is initialized)
        if (offset < instanceTotalCount && instanceTotalCount != -1) {
            final int count = Math.min(PAGE_SIZE, instanceTotalCount - offset);
            load(offset, count);
        }
    }


    private Promise<QueryResult<Projection>> query(int offset, int count) {
        table.getLoadingIndicator().onLoadingStateChanged(LoadingState.LOADING, null);
        InstanceQuery query = new InstanceQuery(Lists.newArrayList(fields), table.buildQueryCriteria(), offset, count);
        return table.getResourceLocator().queryProjection(query);
    }

    /**
     * Loads data and append to table.
     *
     * @param offset offset
     * @param count  count
     */
    private void load(int offset, int count) {
        LOGGER.log(Level.FINE, "Loading instances... offset = " +
                offset + ", count = " + count + ", fields = " + fields);
        query(offset, count).then(new AsyncCallback<QueryResult<Projection>>() {
            @Override
            public void onFailure(Throwable caught) {
                LOGGER.log(Level.SEVERE, "Failed to load instances. fields = " + fields, caught);
                table.getLoadingIndicator().onLoadingStateChanged(LoadingState.FAILED, caught);

                final DataLoadEvent dataLoadEvent = new DataLoadEvent(instanceTotalCount, tableDataProvider.getList().size());
                dataLoadEvent.setFailed(true);
                table.getTable().getEventBus().fireEvent(dataLoadEvent);
            }

            @Override
            public void onSuccess(QueryResult<Projection> result) {
                tableDataProvider.getList().addAll(result.getItems());
                instanceTotalCount = result.getTotalCount();
                table.getTable().getEventBus().fireEvent(new DataLoadEvent(instanceTotalCount, tableDataProvider.getList().size()));
            }
        });
    }

    public void reload() {
        tableDataProvider.getList().clear();
        load(0, PAGE_SIZE);
    }

    public Set<FieldPath> getFields() {
        return fields;
    }
}
