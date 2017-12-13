package org.activityinfo.observable;


import com.google.common.base.Optional;

/**
 * An observable that based on an observable optional value.
 *
 * If the optional value is absent, then this observable will be loading.
 *
 * @param <T>
 */
class FlattenedOptional<T> extends Observable<T> {

    private final Observable<Optional<T>> source;
    private Subscription subscription;

    FlattenedOptional(Observable<Optional<T>> source) {
        this.source = source;
    }

    @Override
    protected void onConnect() {
        subscription = source.subscribe(new Observer<Optional<T>>() {
            @Override
            public void onChange(Observable<Optional<T>> observable) {
                FlattenedOptional.this.fireChange();
            }
        });
    }

    @Override
    protected void onDisconnect() {
        subscription.unsubscribe();
        subscription = null;
    }

    @Override
    public boolean isLoading() {
        return source.isLoading() || !source.get().isPresent();
    }

    @Override
    public T get() {
        return source.get().get();
    }
}
