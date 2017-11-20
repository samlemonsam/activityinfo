package org.activityinfo.observable;

import com.google.common.base.Objects;

class CachedObservable<T> extends Observable<T> {

    private final Observable<T> source;

    private Subscription sourceSubscription;

    private boolean previouslyLoading;
    private T previousValue;

    public CachedObservable(Observable<T> source) {
        this.source = source;
    }

    @Override
    protected void onConnect() {
        this.sourceSubscription = source.subscribe(new Observer<T>() {
            @Override
            public void onChange(Observable<T> observable) {
                if(observable.isLoading()) {
                    if(!previouslyLoading) {
                        previouslyLoading = true;
                        previousValue = null;
                        CachedObservable.this.fireChange();
                    }
                } else {
                    if(previouslyLoading || !Objects.equal(previousValue, observable.get())) {
                        previouslyLoading = false;
                        previousValue = observable.get();
                        CachedObservable.this.fireChange();
                    }
                }
            }
        });
    }

    @Override
    protected void onDisconnect() {
        sourceSubscription.unsubscribe();
        sourceSubscription = null;
        previousValue = null;
    }

    @Override
    public boolean isLoading() {
        return source.isLoading();
    }

    @Override
    public T get() {
        return source.get();
    }
}
