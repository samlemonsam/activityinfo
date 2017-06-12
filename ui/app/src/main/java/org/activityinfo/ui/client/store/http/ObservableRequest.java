package org.activityinfo.ui.client.store.http;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.observable.Observable;


class ObservableRequest<T> extends Observable<T> {

    private final HttpBus bus;
    private final HttpRequest<T> request;
    private T result = null;
    private HttpSubscription subscription;

    public ObservableRequest(HttpBus bus, HttpRequest<T> request) {
        super();
        this.bus = bus;
        this.request = request;
    }


    @Override
    protected void onConnect() {
        if(result == null) {
            subscription = bus.submit(request, new AsyncCallback<T>() {
                @Override
                public void onFailure(Throwable caught) {

                }

                @Override
                public void onSuccess(T result) {
                    ObservableRequest.this.result = result;
                    ObservableRequest.this.subscription = null;
                    ObservableRequest.this.fireChange();
                }
            });
        }
    }

    @Override
    protected void onDisconnect() {
        if(subscription != null) {
            subscription.cancel();
            subscription = null;
        }
    }

    @Override
    public boolean isLoading() {
        return result == null;
    }


    @Override
    public T get() {
        assert !isLoading();
        return result;
    }
}
