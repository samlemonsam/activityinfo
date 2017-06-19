package org.activityinfo.ui.client.store.http;

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.promise.Promise;

public class QueryRequest implements HttpRequest<ColumnSet> {
    private final QueryModel queryModel;

    public QueryRequest(QueryModel queryModel) {
        this.queryModel = queryModel;
    }

    @Override
    public Promise<ColumnSet> execute(ActivityInfoClientAsync client) {
        return client.queryTableColumns(queryModel);
    }
}
