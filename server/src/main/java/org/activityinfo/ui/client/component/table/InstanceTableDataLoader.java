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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.ListDataProvider;
import org.activityinfo.core.client.InstanceQuery;
import org.activityinfo.core.client.QueryResult;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.i18n.shared.I18N;
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

    private static final int PAGE_SIZE = 200;

    private static final Logger LOGGER = Logger.getLogger(InstanceTableDataLoader.class.getName());

    private final ListDataProvider<Projection> tableDataProvider = new ListDataProvider<>();
    private final InstanceTable table;
    private final Set<FieldPath> fields = Sets.newHashSet();

    private int lastVerticalScrollPosition;
    private int instanceTotalCount = -1;

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

        // if near the end then load data
        if (lastVerticalScrollPosition >= (maxScrollTop - 20)) {
            loadMore();
        }
    }

    public void loadMore() {
        if (!isAllLoaded()) {
            int offset = offset();
            final int countToLoad = Math.min(PAGE_SIZE, instanceTotalCount - offset);
            load(offset, countToLoad);
        }
    }


    private Promise<QueryResult<Projection>> query(int offset, int countToLoad) {
        InstanceQuery query = new InstanceQuery(Lists.newArrayList(fields), table.buildQueryCriteria(), offset, countToLoad);
        return table.getResourceLocator().queryProjection(query);
    }

    public boolean isAllLoaded() {
        return offset() >= instanceTotalCount && instanceTotalCount != -1;
    }

    private int offset() {
        return tableDataProvider.getList().size();
    }

    /**
     * Loads data and append to table.
     *
     * @param offset offset
     * @param countToLoad  count
     */
    private void load(final int offset, int countToLoad) {
        LOGGER.log(Level.FINE, "Loading instances... offset = " +
                offset + ", count = " + countToLoad + ", totalCount = " + instanceTotalCount +", fields = " + fields);

        if (table.getLoadingIndicator().isLoading()) {
            LOGGER.log(Level.FINE, "Loading already in progress. Skip!");
            return;
        }

        if (isAllLoaded()) {
            LOGGER.log(Level.FINE, "All data are already loaded.");
            return;
        }

        table.getLoadingIndicator().onLoadingStateChanged(LoadingState.LOADING, I18N.CONSTANTS.loading());

        query(offset, countToLoad).then(new AsyncCallback<QueryResult<Projection>>() {
            @Override
            public void onFailure(Throwable caught) {
                LOGGER.log(Level.SEVERE, "Failed to load instances. fields = " + fields, caught);
                table.getLoadingIndicator().onLoadingStateChanged(LoadingState.FAILED, caught);
            }

            @Override
            public void onSuccess(QueryResult<Projection> result) {
                table.getLoadingIndicator().onLoadingStateChanged(LoadingState.LOADED);
                tableDataProvider.getList().addAll(result.getProjections());

                InstanceTableDataLoader.this.instanceTotalCount = result.getTotalCount();
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

    public void loadAll() {
        load(offset(), Integer.MAX_VALUE);
    }
}
