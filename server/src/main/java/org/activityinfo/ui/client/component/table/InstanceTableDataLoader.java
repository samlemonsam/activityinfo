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
import org.activityinfo.core.client.InstanceQueryResult;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.core.shared.form.tree.FieldPath;
import org.activityinfo.fp.client.Promise;
import org.activityinfo.ui.client.widget.CellTable;
import org.activityinfo.ui.client.widget.loading.LoadingState;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author yuriyz on 4/10/14.
 */
public class InstanceTableDataLoader {

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

            final int offset = tableDataProvider.getList().size();

            // load data only if offset is less then total count (and totalCount is initialized)
            if (offset < instanceTotalCount && instanceTotalCount != -1) {
                final int count = Math.min(InstanceTable.PAGE_SIZE, instanceTotalCount - offset);
                load(offset, count);
            }
        }
    }


    private Promise<InstanceQueryResult> query(int offset, int count) {
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
        query(offset, count).then(new AsyncCallback<InstanceQueryResult>() {
            @Override
            public void onFailure(Throwable caught) {
                LOGGER.log(Level.SEVERE, "Failed to load instances. fields = " + fields, caught);
                table.getLoadingIndicator().onLoadingStateChanged(LoadingState.FAILED, caught);
            }

            @Override
            public void onSuccess(InstanceQueryResult result) {
                tableDataProvider.getList().addAll(result.getProjections());
                instanceTotalCount = result.getTotalCount();
            }
        });
    }

    public void reload() {
        tableDataProvider.getList().clear();
        load(0, InstanceTable.PAGE_SIZE);
    }

    public Set<FieldPath> getFields() {
        return fields;
    }
}
