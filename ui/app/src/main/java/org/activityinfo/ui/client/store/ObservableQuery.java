package org.activityinfo.ui.client.store;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.http.HttpBus;
import org.activityinfo.ui.client.http.HttpSubscription;
import org.activityinfo.ui.client.http.QueryRequest;


public class ObservableQuery extends Observable<ColumnSet> {

    private HttpBus httpBus;
    private QueryModel queryModel;
    private ColumnSet columnSet;
    private HttpSubscription httpSubscription;

    public ObservableQuery(HttpBus httpBus, QueryModel queryModel) {
        this.httpBus = httpBus;
        this.queryModel = queryModel;
    }

    @Override
    protected void onConnect() {
        if (columnSet == null) {
            this.httpSubscription = httpBus.submit(new QueryRequest(queryModel), new AsyncCallback<ColumnSet>() {
                @Override
                public void onFailure(Throwable caught) {

                }

                @Override
                public void onSuccess(ColumnSet result) {
                    ObservableQuery.this.columnSet = result;
                    ObservableQuery.this.httpSubscription = null;
                    fireChange();
                }
            });
        }

    }

    @Override
    public boolean isLoading() {
        return columnSet == null;
    }

    @Override
    public ColumnSet get() {
        assert !isLoading();
        return columnSet;
    }

    @Override
    protected void onDisconnect() {
        if (httpSubscription != null) {
            httpSubscription.cancel();
            httpSubscription = null;
        }
    }
}