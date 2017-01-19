package org.activityinfo.observable;

import java.util.ArrayList;
import java.util.List;


public abstract class ObservableList<T> {
    private final List<ListObserver<T>> observers = new ArrayList<>();

    public abstract boolean isLoading();

    public final Subscription subscribe(final ListObserver<T> observer) {

        observers.add(observer);

        return new Subscription() {
            @Override
            public void unsubscribe() {
                observers.remove(observer);
            }
        };
    }

    /**
     * Signal that the list has changed.
     */
    protected final void fireChanged() {
        for (ListObserver<T> observer : observers) {
            observer.onChange();
        }
    }


    /**
     * Signal that the given {@code element} has been added to the list
     */
    protected final void fireAdded(T element) {
        for (ListObserver<T> observer : observers) {
            observer.onElementAdded(element);
        }
    }

    /**
     * Signal that the given {@code element} has been removed from the list
     */
    protected final void fireRemoved(T element) {
        for (ListObserver<T> observer : observers) {
            observer.onElementRemoved(element);
        }
    }

    public abstract List<T> asList();

    public final Observable<List<T>> asObservable() {
        return new Observable<List<T>>() {

            private Subscription subscription;

            @Override
            public boolean isLoading() {
                return ObservableList.this.isLoading();
            }

            @Override
            public List<T> get() {
                return ObservableList.this.asList();
            }

            @Override
            protected void onConnect() {
                final Observable<List<T>> thisObservable = this;
                subscription = ObservableList.this.subscribe(new ListObserver<T>() {
                    @Override
                    public void onChange() {
                        thisObservable.fireChange();
                    }

                    @Override
                    public void onElementAdded(T element) {
                        thisObservable.fireChange();
                    }

                    @Override
                    public void onElementRemoved(T element) {
                        thisObservable.fireChange();
                    }
                });
            }

            @Override
            protected void onDisconnect() {
                subscription.unsubscribe();
            }
        };
    }
}
