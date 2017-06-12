package org.activityinfo.ui.client.store;

import org.activityinfo.observable.Observable;
import org.activityinfo.observable.SubscriptionSet;

import java.util.Comparator;

class Best<T> extends Observable<T> {

    private final Observable<T> x;
    private final Observable<T> y;
    private final Comparator<T> comparator;
    private SubscriptionSet subscriptions;

    private T value = null;

    public Best(Observable<T> x, Observable<T> y, Comparator<T> comparator) {
        this.x = x;
        this.y = y;
        this.comparator = comparator;
    }

    @Override
    protected void onConnect() {
        super.onConnect();
        subscriptions = new SubscriptionSet();
        subscriptions.add(x.subscribe(this::onChange));
        subscriptions.add(y.subscribe(this::onChange));
    }

    @Override
    protected void onDisconnect() {
        subscriptions.unsubscribeAll();
        subscriptions = null;
    }

    private void onChange(Observable<T> observable) {
        if(observable.isLoaded()) {
            if(value == null) {
                value = observable.get();
                fireChange();
            } else if(comparator.compare(observable.get(), value) > 0) {
                value = observable.get();
                fireChange();
            }
        }
    }

    @Override
    public boolean isLoading() {
        return value == null;
    }

    @Override
    public T get() {
        assert value != null : "not loaded!";
        return value;
    }
}
