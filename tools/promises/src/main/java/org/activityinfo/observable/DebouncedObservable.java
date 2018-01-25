package org.activityinfo.observable;

import com.google.gwt.user.client.Timer;

class DebouncedObservable<T> extends Observable<T> {

    private final Observable<T> source;
    private final int delay;
    private final Timer timer;
    private Subscription sourceSubscription;

    public DebouncedObservable(Observable<T> source, int delay) {
        this.source = source;
        this.delay = delay;
        this.timer = new Timer() {
            @Override
            public void run() {
                DebouncedObservable.this.fireChange();
            }
        };
    }

    @Override
    protected void onConnect() {
        sourceSubscription = source.subscribe(new Observer<T>() {
            @Override
            public void onChange(Observable<T> observable) {
                if(timer.isRunning()) {
                    timer.cancel();
                }
                timer.schedule(delay);
            }
        });
    }

    @Override
    protected void onDisconnect() {
        sourceSubscription.unsubscribe();
        sourceSubscription = null;
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
