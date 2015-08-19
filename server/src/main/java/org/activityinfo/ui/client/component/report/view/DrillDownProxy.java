package org.activityinfo.ui.client.component.report.view;

import com.extjs.gxt.ui.client.data.RpcProxy;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.client.Dispatcher;
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
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    @Override
    protected void load(Object loadConfig, final AsyncCallback<List<DrillDownRow>> callback) {
        PivotSites query = new PivotSites(dims, filter);
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
            row.set("location", getEntity(bucket, locationDimension).getLabel());
            row.set("date", getDate(bucket));
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
