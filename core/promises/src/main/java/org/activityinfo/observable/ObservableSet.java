package org.activityinfo.observable;


import java.util.ArrayList;
import java.util.List;

public abstract class ObservableSet<T>  {

    private final List<SetObserver<T>> observers = new ArrayList<>();
    
    public abstract boolean isLoading();
    
    public final Subscription subscribe(final SetObserver<T> observer) {
        
        observers.add(observer);
        
        return new Subscription() {
            @Override
            public void unsubscribe() {
                observers.remove(observer);
            }
        };
    }
    
    /**
     * Signal that the given {@code element} has been added to the set
     */
    protected final void fireChanged() {
        for (SetObserver<T> observer : observers) {
            observer.onChange();
        }
    }    
    
    
    /**
     * Signal that the given {@code element} has been added to the set
     */
    protected final void fireAdded(T element) {
        for (SetObserver<T> observer : observers) {
            observer.onElementAdded(element);
        }
    }

    /**
     * Signal that the given {@code element} has been removed from the set
     */
    protected final void fireRemoved(T element) {
        for (SetObserver<T> observer : observers) {
            observer.onElementRemoved(element);
        }
    }
     
}
