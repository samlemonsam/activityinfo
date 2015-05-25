package org.activityinfo.observable;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import org.activityinfo.promise.BiFunction;

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
    
    public abstract T get();

    public final Subscription subscribe(final Observer<T> observer) {
        if(observers.isEmpty()) {
            onConnect();
        }
        observer.onChange(this);
        
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
        for(Observer<T> observer : observers) {
            observer.onChange(this);
        }
    }
    
    public <R> Observable<R> transform(final Function<T, R> function) {
        return new ObservableFunction<R>(this) {
            @Override
            protected R compute(Object[] arguments) {
                T argumentValue = (T) arguments[0];
                return Preconditions.checkNotNull(function.apply(argumentValue));
            }
        };
    }
    
    public static <T, U, R> Observable<R> transform(Observable<T> t, Observable<U> u, final BiFunction<T, U, R> function) {
        return new ObservableFunction<R>(t, u) {

            @Override
            @SuppressWarnings("unchecked")
            protected R compute(Object[] arguments) {
                T t = (T)arguments[0];
                U u = (U)arguments[1];
                return function.apply(t, u);
            }
        };
    }
    
    public <R> Observable<R> join(final Function<T, Observable<R>> function) {
        return new ChainedObservable<>(transform(function));
    }
}
