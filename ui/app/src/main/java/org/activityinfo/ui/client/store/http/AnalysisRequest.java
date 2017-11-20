package org.activityinfo.ui.client.store.http;

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.model.analysis.Analysis;
import org.activityinfo.promise.Maybe;
import org.activityinfo.promise.Promise;

public class AnalysisRequest implements HttpRequest<Maybe<Analysis>> {

    private String id;

    public AnalysisRequest(String id) {
        this.id = id;
    }


    @Override
    public Promise<Maybe<Analysis>> execute(ActivityInfoClientAsync client) {
        return client.getAnalysis(id);
    }

    @Override
    public int refreshInterval(Maybe<Analysis> result) {
        return 0;
    }

}
