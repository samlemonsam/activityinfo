package org.activityinfo.observable;


public class ChainedObservable<T> extends Observable<T> {

    private Observable<Observable<T>> observable = null;
    private Observable<T> currentValue = null;

    private Subscription observableSubscription = null;
    private Subscription valueSubscription = null;

    public ChainedObservable(Observable<Observable<T>> value) {
        this.observable = value;
    }

    @Override
    public boolean isLoading() {
        return observable.isLoading() || observable.get().isLoading();
    }

    @Override
    public T get() {
        return observable.get().get();
    }

    @Override
    protected void onConnect() {
        assert observableSubscription == null;

        observableSubscription = observable.subscribe(new Observer<Observable<T>>() {
            @Override
            public void onChange(Observable<Observable<T>> observable) {
                unsubscribeFromOldValue();
                if(!observable.isLoading()) {
                    subscribeToNewValue(observable.get());   
                }
            }
        });
    }

    private void unsubscribeFromOldValue() {
        // Unsubscribe from the old value if we had previously been listening
        if(valueSubscription != null) {
            valueSubscription.unsubscribe();
            valueSubscription = null;
        }
    }

    private void subscribeToNewValue(Observable<T> value) {
        valueSubscription = value.subscribe(new Observer<T>() {
            @Override
            public void onChange(Observable<T> observable) {
                ChainedObservable.this.fireChange();
            }
        });
    }
    
    @Override
    protected void onDisconnect() {
        assert observableSubscription != null;

        observableSubscription.unsubscribe();

        if(valueSubscription != null) {
            valueSubscription.unsubscribe();
        }
    }
}
