/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.component.report.view;

import com.extjs.gxt.ui.client.data.RpcProxy;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.PivotSites;
import org.activityinfo.legacy.shared.command.result.Bucket;
import org.activityinfo.legacy.shared.reports.content.DayCategory;
import org.activityinfo.legacy.shared.reports.content.EntityCategory;
import org.activityinfo.legacy.shared.reports.model.DateDimension;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.date.DateUnit;
import org.activityinfo.ui.client.dispatch.Dispatcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads drill down rows
 */
public class DrillDownProxy extends RpcProxy<List<DrillDownRow>> {
    private Dispatcher dispatcher;
    private Filter filter;

    private final Dimension siteDimension = new Dimension(DimensionType.Site);
    private final Dimension partnerDimension = new Dimension(DimensionType.Partner);
    private final Dimension locationDimension = new Dimension(DimensionType.Location);
    private final DateDimension dateDimension = new DateDimension(DateUnit.DAY);
    private final Dimension databaseDimension = new Dimension(DimensionType.Database);
    private final Dimension actvitiyDimension = new Dimension(DimensionType.Activity);
    private final Dimension indicatorDimension = new Dimension(DimensionType.Indicator);
    private final Set<Dimension> dims;

    public DrillDownProxy(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;

        dims = new HashSet<>();
        dims.add(siteDimension);
        dims.add(partnerDimension);
        dims.add(locationDimension);
        dims.add(dateDimension);
        dims.add(indicatorDimension);
        dims.add(databaseDimension);
        dims.add(actvitiyDimension);
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    @Override
    protected void load(Object loadConfig, final AsyncCallback<List<DrillDownRow>> callback) {
        PivotSites query = new PivotSites(dims, filter);

        if (query.isTooBroad()) {
            callback.onSuccess(Lists.<DrillDownRow>newArrayList());
            return;
        }

        dispatcher.execute(query, new AsyncCallback<PivotSites.PivotResult>() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(PivotSites.PivotResult result) {
                try {
                    callback.onSuccess(toRows(result));
                } catch (Throwable caught) {
                    Log.error(caught.getMessage(), caught);
                    callback.onFailure(caught);
                }
            }
        });
    }

    private List<DrillDownRow> toRows(PivotSites.PivotResult result) {
        List<DrillDownRow> rows = new ArrayList<>();
        for (Bucket bucket : result.getBuckets()) {
            DrillDownRow row = new DrillDownRow(getEntity(bucket, siteDimension).getId());
            row.set("partner", getEntity(bucket, partnerDimension).getLabel());
            
            EntityCategory location = getEntity(bucket, locationDimension);
            if(location != null) {
                row.set("location", location.getLabel());
            }
            row.set("date", getDate(bucket));
            row.set("database", getEntity(bucket, databaseDimension).getLabel());
            row.set("activity", getEntity(bucket, actvitiyDimension).getLabel());
            row.set("indicator", getEntity(bucket, indicatorDimension).getLabel());
            row.set("value", bucket.doubleValue());
            rows.add(row);
        }
        return rows;
    }

    private String getDate(Bucket bucket) {
        DayCategory dayCategory = (DayCategory) bucket.getCategory(dateDimension);
        return dayCategory.getLabel();
    }

    private EntityCategory getEntity(Bucket bucket, Dimension dim) {
        return ((EntityCategory) bucket.getCategory(dim));
    }

}
