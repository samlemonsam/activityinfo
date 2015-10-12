package org.activityinfo.ui.client.component.report.editor.pivotTable;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.PivotSites;
import org.activityinfo.legacy.shared.impl.pivot.PivotTableDataBuilder;
import org.activityinfo.legacy.shared.reports.content.PivotContent;
import org.activityinfo.legacy.shared.reports.content.PivotTableData;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.legacy.shared.reports.model.PivotReportElement;
import org.activityinfo.legacy.shared.reports.model.PivotTableReportElement;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.component.report.view.ReportView;
import org.activityinfo.ui.client.component.report.view.ReportViewBinder;

import java.util.HashSet;
import java.util.Set;


public class PivotTableBinder extends ReportViewBinder<PivotContent, PivotReportElement<PivotContent>> {
    
    private Set<Dimension> cachedDimensions;
    private Filter cachedFilter;
    private PivotSites.PivotResult cachedResult;
    private Dispatcher dispatcher;

    public PivotTableBinder(EventBus eventBus, Dispatcher dispatcher, ReportView<PivotReportElement<PivotContent>> view) {
        super(eventBus, dispatcher, view);
        this.dispatcher = dispatcher;
    }

    @Override
    protected void executeLoad(final AsyncCallback<PivotContent> callback) {

        final Filter filter = new Filter(getModel().getFilter());
        final Set<Dimension> dimensions = new HashSet<>(getModel().allDimensions());
        
        if(filter.equals(cachedFilter) && dimensions.equals(cachedDimensions)) {
            try {
                callback.onSuccess(generateElement());
            } catch (Exception e) {
                callback.onFailure(e);
            }
        } else {

            dispatcher.execute(new PivotSites(dimensions, filter), new AsyncCallback<PivotSites.PivotResult>() {
                @Override
                public void onFailure(Throwable throwable) {
                    callback.onFailure(throwable);
                }

                @Override
                public void onSuccess(PivotSites.PivotResult pivotResult) {
                    cachedFilter = filter;
                    cachedDimensions = dimensions;
                    cachedResult = pivotResult;

                    try {
                        callback.onSuccess(generateElement());
                    } catch (Exception e) {
                        callback.onFailure(e);
                    }
                }
            });
        }
    }

    private boolean isCached(Filter filter, Set<Dimension> dimensions) {
        return filter.equals(cachedFilter) && dimensions.equals(cachedDimensions);
    }


    private PivotContent generateElement() {
        
        PivotTableReportElement model = (PivotTableReportElement) getModel();

        PivotTableDataBuilder builder = new PivotTableDataBuilder();
        PivotTableData data = builder.build(
                model.getRowDimensions(), 
                model.getColumnDimensions(),
                cachedResult.getBuckets());

        PivotContent content = new PivotContent();
        content.setEffectiveFilter(cachedFilter);
        content.setData(data);
        
        return content;
    }

}
