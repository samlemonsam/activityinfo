package org.activityinfo.ui.client.store.http;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.FormChange;
import org.activityinfo.ui.client.store.FormChangeEvent;
import org.activityinfo.ui.client.store.FormChangeEventHandler;

import java.util.function.Predicate;


class ObservableRequest<T> extends Observable<T> {

    private final EventBus eventBus;
    private final HttpBus bus;
    private final HttpRequest<T> request;
    private final Predicate<FormChange> refetchPredicate;

    private HttpSubscription httpSubscription;
    private HandlerRegistration eventBusRegistration;

    private T result = null;

    public ObservableRequest(EventBus eventBus, HttpBus bus, HttpRequest<T> request,
                             Predicate<FormChange> refetchPredicate) {
        super();
        this.eventBus = eventBus;
        this.bus = bus;
        this.request = request;
        this.refetchPredicate = refetchPredicate;
    }


    @Override
    protected void onConnect() {
        if(result == null) {
            startFetch();
        }

        eventBusRegistration = eventBus.addHandler(FormChangeEvent.TYPE, new FormChangeEventHandler() {
            @Override
            public void onFormChange(FormChangeEvent event) {
                if (!pendingHttpRequest() && refetchPredicate.test(event.getPredicate())) {
                    // Our current result has become outdated, we need to fetch a new version from
                    // the server
                    refetch();
                }
            }

        });
    }


    private boolean pendingHttpRequest() {
        if(httpSubscription == null) {
            return false;
        }
        return httpSubscription.isPending();

    }

    private void startFetch() {
        httpSubscription = bus.submit(request, new AsyncCallback<T>() {
            @Override
            public void onFailure(Throwable caught) {

            }

            @Override
            public void onSuccess(T result) {
                ObservableRequest.this.result = result;
                ObservableRequest.this.httpSubscription = null;
                ObservableRequest.this.fireChange();
            }
        });
    }

    /**
     * When the user's activity on the client side has affected the remote state,
     * for example, when an update is submitted, then we have to refetch the result.
     */
    private void refetch() {
        result = null;
        fireChange();

        startFetch();
    }

    @Override
    protected void onDisconnect() {
        if(httpSubscription != null) {
            httpSubscription.cancel();
            httpSubscription = null;
        }

        eventBusRegistration.removeHandler();
        eventBusRegistration = null;
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
