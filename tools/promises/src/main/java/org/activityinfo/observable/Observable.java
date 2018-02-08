package org.activityinfo.observable;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.shared.GwtIncompatible;
import org.activityinfo.promise.Function2;
import org.activityinfo.promise.Function3;
import org.activityinfo.promise.Promise;

import java.util.ArrayList;
import java.util.Arrays;
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

    public final boolean isLoaded() {
        return !isLoading();
    }

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
                boolean removed = observers.remove(observer);
                assert removed : "Already unsubscribed!";
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
     * Notify subscribers that the value has changed by invoking the 
     * {@link org.activityinfo.observable.Observer#onChange(Observable)} 
     * method of all subscribed {@link org.activityinfo.observable.Observer}s.
     */
    protected final void fireChange() {
        if(!observers.isEmpty()) {
            List<Observer<T>> toNotify = new ArrayList<>(observers);
            for (Observer<T> observer : toNotify) {
                observer.onChange(this);
            }
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
     * @param function a function that is applied to the current any subsequent value of this {@code Observable}  
     * @return a new {@code Observable}
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

    public final <R> Observable<R> transformIf(Function<T, Optional<R>> function) {
        return new FlattenedOptional<>(transform(function));
    }

    public static <T, U, R> Observable<R> transform(Observable<T> t, Observable<U> u, final Function2<T, U, R> function) {
        return transform(SynchronousScheduler.INSTANCE, t, u, function);
    }

    public static <T, U, R> Observable<R> transform(Scheduler scheduler, Observable<T> t, Observable<U> u, final Function2<T, U, R> function) {
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

    public static <A, B, C, R> Observable<R> transform(Observable<A> a, Observable<B> b, Observable<C> c,
                                                       final Function3<A, B, C, R> function) {
        return transform(SynchronousScheduler.INSTANCE, a, b, c, function);
    }

    public static <A, B, C, R> Observable<R> transform(Scheduler scheduler, Observable<A> a, Observable<B> b, Observable<C> c,
                                                       final Function3<A, B, C, R> function) {
        return new ObservableFunction<R>(scheduler, a, b, c) {

            @Override
            @SuppressWarnings("unchecked")
            protected R compute(Object[] arguments) {
                A a = (A) arguments[0];
                B b = (B) arguments[1];
                C c = (C) arguments[2];
                return function.apply(a, b, c);
            }
        };
    }



    public <R> Observable<R> join(final Function<T, Observable<R>> function) {
        return new ChainedObservable<>(transform(function));
    }

    public static <X, Y, R> Observable<R> join(Observable<X> x, Observable<Y> y, Function2<X, Y, Observable<R>> function) {
        return new ChainedObservable<>(transform(x, y, function));
    }

    public static <X, Y, Z, R> Observable<R> join(Observable<X> x, Observable<Y> y, Observable<Z> z, Function3<X, Y, Z, Observable<R>> function) {
        return new ChainedObservable<>(transform(SynchronousScheduler.INSTANCE, x, y, z, function));
    }

    public static <T> Observable<T> just(T value) {
        return new ConstantObservable<>(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> Observable<T> loading() {
        return (Observable<T>) Never.INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public static <T> Observable<List<T>> flatten(Scheduler scheduler, List<Observable<T>> list) {
        return new ObservableFunction<List<T>>(scheduler, (List)list) {
            @Override
            protected List<T> compute(Object[] arguments) {
                return (List<T>) Arrays.asList(arguments);
            }
        };
    }

    public static <T> Observable<List<T>> flatten(ObservableList<Observable<T>> list) {
        return new ObservableFlatMap<>(list);
    }

    @SuppressWarnings("unchecked")
    public static <T> Observable<List<T>> flatten(List<Observable<T>> list) {
        return flatten(SynchronousScheduler.INSTANCE, list);
    }

    /**
     * Given a collection which is observable, apply the function {@code f} to each of its elements, and join the results
     * in a new list which is itself observable.
     *
     */
    public static <T, TT extends Iterable<T>, R> Observable<List<R>> flatMap(Observable<TT> observableCollection, final Function<T, Observable<R>> f) {
        return observableCollection.join(new Function<TT, Observable<List<R>>>() {
            @Override
            public Observable<List<R>> apply(TT collection) {
                List < Observable < R >> list = new ArrayList<>();
                for (T element : collection) {
                    list.add(f.apply(element));
                }
                return flatten(list);
            }
        });
    }

    /**
     * Given a list of inputs, apply the given {@code function} to each element, then flatten the resulting list
     * of obserables into an observable list.
     */
    public static <T, R> Observable<List<R>> flatJoin(List<T> inputList, Function<T, Observable<R>> function) {
        List<Observable<R>> applied = new ArrayList<>();
        for (T t : inputList) {
            applied.add(function.apply(t));
        }
        return flatten(applied);
    }

    public static <T> Observable<T> flattenOptional(Observable<Optional<T>> observable) {
        return observable.join(new Function<Optional<T>, Observable<T>>() {
            @Override
            public Observable<T> apply(Optional<T> value) {
                if(value.isPresent()) {
                    return Observable.just(value.get());
                } else {
                    return Observable.loading();
                }
            }
        });
    }

    @GwtIncompatible
    public final T waitFor() {
        final List<T> collector = new ArrayList<>();
        Subscription subscription = this.subscribe(new Observer<T>() {
            @Override
            public void onChange(Observable<T> observable) {
                if (observable.isLoaded()) {
                    collector.add(observable.get());
                }
            }
        });
        if(collector.isEmpty()) {
            throw new IllegalStateException("Did not load synchronously.");
        }
        subscription.unsubscribe();
        return collector.get(0);
    }

    public final Promise<T> once() {
        final Promise<T> result = new Promise<>();
        final Promise<Subscription> pendingSubscription = new Promise<>();
        final Subscription subscription = this.subscribe(new Observer<T>() {
            @Override
            public void onChange(Observable<T> observable) {
                if(observable.isLoaded()) {
                    result.resolve(observable.get());

                    if (pendingSubscription.isSettled()) {
                        pendingSubscription.get().unsubscribe();
                    }
                }
            }
        });

        if(result.isSettled()) {
            subscription.unsubscribe();
        } else {
            pendingSubscription.resolve(subscription);
        }

        return result;
    }

    /**
     * Returns a new {@code Observable} that will only fire change notification values
     * when the source's value has actually changed.
     *
     * <p>Note that {@code T} <strong>must</strong> be immutable! If {@code T} includes mutable state,
     * change detection can fail!!!</p>
     *
     */
    public final Observable<T> cache() {
        return new CachedObservable<>(this);
    }

    public final Observable<T> debounce(int milliseconds) {
        if(!GWT.isClient()) {
            return this;
        } else {
            return new DebouncedObservable<>(this, milliseconds);
        }
    }
}
