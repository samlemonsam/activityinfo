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

    /**
     * @return true if the value is being loaded across the network, is being calculated, or
     * is otherwise not yet available. 
     */
    public abstract boolean isLoading();

    /**
     * 
     * @return the current value
     * @throws java.lang.IllegalStateException if the value is currently loading
     */
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

    /**
     * Called when the first {@link org.activityinfo.observable.Observer} subscribes to notifications.
     *
     */
    protected void onConnect() {
        
    }

    /**
     * Called when the last {@link org.activityinfo.observable.Observer} unsubscribes.
     */
    protected void onDisconnect() {

    }

    /**
     * Notify subscribers that the value has changed by invoking the {@link org.activityinfo.observable.Observer#onChange(Observable)} 
     * method of all subscribed {@link org.activityinfo.observable.Observer}s.
     */
    protected final void fireChange() {
        for(Observer<T> observer : observers) {
            observer.onChange(this);
        }
    }

    /**
     * Transforms this {@code Observable}'s using the given {@code function} 
     * @param function a function that is applied to the current any subsequent value of this {@code Observable}
     * @param <R> the type of the result returned by the given {@code function}
     * @return a new {@code Observable}
     */
    public final <R> Observable<R> transform(final Function<T, R> function) {
        return transform(SynchronousScheduler.INSTANCE, function);
    }

    /**
     * Transforms this {@code Observable}'s using the given {@code function} 
     * @param <R> the type of the result returned by the given {@code function}
     * @param scheduler
     *@param function a function that is applied to the current any subsequent value of this {@code Observable}  @return a new {@code Observable}
     */
    public final <R> Observable<R> transform(Scheduler scheduler, final Function<T, R> function) {
        return new ObservableFunction<R>(scheduler, this) {
            @Override
            @SuppressWarnings("unchecked")
            protected R compute(Object[] arguments) {
                T argumentValue = (T) arguments[0];
                return Preconditions.checkNotNull(function.apply(argumentValue));
            }
        };
    }

    public static <T, U, R> Observable<R> transform(Observable<T> t, Observable<U> u, final BiFunction<T, U, R> function) {
        return transform(SynchronousScheduler.INSTANCE, t, u, function);
    }

    public static <T, U, R> Observable<R> transform(Scheduler scheduler, Observable<T> t, Observable<U> u, final BiFunction<T, U, R> function) {
        return new ObservableFunction<R>(scheduler, t, u) {

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
