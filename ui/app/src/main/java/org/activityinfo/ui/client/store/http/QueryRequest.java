package org.activityinfo.ui.client.store.http;

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.query.RowSource;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.FormChange;

public class QueryRequest implements HttpRequest<ColumnSet> {
    private final QueryModel queryModel;

    public QueryRequest(QueryModel queryModel) {
        this.queryModel = queryModel;
    }

    @Override
    public Promise<ColumnSet> execute(ActivityInfoClientAsync client) {
        return client.queryTableColumns(queryModel);
    }

    @Override
    public boolean shouldRefresh(FormChange change) {
        // TODO: we need to check for related tables as well...
        for (RowSource rowSource : queryModel.getRowSources()) {
            if(change.isFormChanged(rowSource.getRootFormId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int refreshInterval(ColumnSet result) {
        return -1;
    }
}
