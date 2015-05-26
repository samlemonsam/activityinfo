package org.activityinfo.observable;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class ObservableSet<T> extends Observable<Set<T>>  {

    private final List<SetObserver<T>> setObservers = new ArrayList<>();
    
    public final Subscription subscribe(final SetObserver<T> observer) {
        return new Subscription() {
            @Override
            public void unsubscribe() {
                setObservers.remove(observer);
            }
        };
    }
    
    /**
     * Signal that the given {@code element} has been added to the set
     */
    protected final void fireAdded(T element) {
        for (SetObserver<T> observer : setObservers) {
            observer.onElementAdded(element);
        }
    }

    /**
     * Signal that the given {@code element} has been removed from the set
     */
    protected final void fireRemoved(T element) {
        for (SetObserver<T> observer : setObservers) {
            observer.onElementRemoved(element);
        }
    }
     
}
