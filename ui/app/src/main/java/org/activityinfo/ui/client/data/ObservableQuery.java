package org.activityinfo.ui.client.data;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Promise;

/**
 * Created by alex on 19-1-17.
 */
public class ObservableQuery extends Observable<ColumnSet> {

    private QueryModel queryModel;
    private Promise<ColumnSet> result = null;

    public ObservableQuery(ActivityInfoClientAsync client, QueryModel queryModel) {
        this.queryModel = queryModel;
        this.result = client.queryTableColumns(queryModel);
        this.result.then(new AsyncCallback<ColumnSet>() {
            @Override
            public void onFailure(Throwable caught) {

            }

            @Override
            public void onSuccess(ColumnSet result) {
                fireChange();
            }
        });
    }

    @Override
    public boolean isLoading() {
        return result.getState() != Promise.State.FULFILLED;
    }

    @Override
    public ColumnSet get() {
        assert !isLoading();
        return result.get();
    }
}
