package org.activityinfo.promise;

import java.util.ArrayList;
import java.util.List;

/**
 * The Observable class that implements the Reactive Pattern.
 * <p>
 * This class provides methods for subscribing to the Observable 
 * @param <T>
 *            the type of the items emitted by the Observable
 */
public abstract class Observable<T> {
    
    private final List<Observer<T>> observers = new ArrayList<>();
    
    public abstract boolean isLoading();
    
    public abstract T getValue();

    public final Subscription subscribe(final Observer<T> observer) {
        if(observers.isEmpty()) {
            onConnect();
        }
        observers.add(observer);
        return new Subscription() {
            @Override
            public void unsubscribe() {
                observers.remove(observer);   
                if(observers.isEmpty()) {
                    onDisconnect();
                }
            }
        };
    }

    protected void onConnect() {
        
    }

    protected void onDisconnect() {

    }    
    
    protected final void fireChange() {
        for(Observer observer : observers) {
            observer.onChange(this);
        }
    }
}
