package org.activityinfo.promise;


import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * The Promise interface represents a proxy for a value not necessarily known at its creation time.
 * It allows you to associate handlers to an asynchronous action's eventual success or failure.
 * This let asynchronous methods to return values like synchronous methods: instead of the final value,
 * the asynchronous method returns a promise of having a value at some point in the future.
 *
 * @param <T> the type of the promised value
 */
public final class Promise<T> implements AsyncCallback<T> {


    public enum State {

        /**
         * The action relating to the promise succeeded
         */
        FULFILLED,

        /**
         * The action relating to the promise failed
         */
        REJECTED,

        /**
         * Hasn't fulfilled or rejected yet
         */
        PENDING
    }

    private State state = State.PENDING;
    private T value;
    private Throwable exception;

    private List<AsyncCallback<? super T>> callbacks = null;

    public Promise() {
    }

    public State getState() {
        return state;
    }

    public boolean isSettled() {
        return state == State.FULFILLED || state == State.REJECTED;
    }

    public final void resolve(T value) {
        if (state != State.PENDING) {
            return;
        }
        this.value = value;
        this.state = State.FULFILLED;

        publishFulfillment();
    }

    public void then(AsyncCallback<? super T> callback) {
        switch (state) {
            case PENDING:
                if (callbacks == null) {
                    callbacks = Lists.newArrayList();
                }
                callbacks.add(callback);
                break;
            case FULFILLED:
                callback.onSuccess(value);
                break;
            case REJECTED:
                callback.onFailure(exception);
                break;
        }
    }

    public <R> Promise<R> join(final Function<? super T, Promise<R>> function) {
        final Promise<R> chained = new Promise<R>();
        then(new AsyncCallback<T>() {
            @Override
            public void onFailure(Throwable caught) {
                chained.onFailure(caught);
            }

            @Override
            public void onSuccess(T t) {
                try {
                    function.apply(t).then(chained);
                } catch(Throwable caught) {
                    chained.onFailure(caught);
                }
            }
        });
        return chained;
    }


    public Promise<Void> thenDiscardResult() {
        return then(Functions.<Void>constant(null));
    }

    public <R> Promise<R> join(Supplier<Promise<R>> supplier) {
        return join(Functions.forSupplier(supplier));
    }

    /**
     * Provides state updates to the given monitor.
     * @return {@code this}, for method chaining
     */
    public Promise<T> withMonitor(final PromiseMonitor monitor) {
        monitor.onPromiseStateChanged(state);
        if(state == State.PENDING) {
            then(new AsyncCallback<T>() {
                @Override
                public void onFailure(Throwable caught) {
                    monitor.onPromiseStateChanged(State.REJECTED);
                }

                @Override
                public void onSuccess(T result) {
                    monitor.onPromiseStateChanged(State.FULFILLED);
                }
            });
        }
        return this;
    }

    /**
     * Transforms a binary function {@code (T, U) → R} to a function which operates on the equivalent
     * Promised values: {@code ( Promise<T>, Promise<U> ) → Promise<R> }
     */
    public static <T, U, R> BiFunction<Promise<T>, Promise<U>, Promise<R>> fmap(final BiFunction<T, U, R> function) {
        return new BiFunction<Promise<T>, Promise<U>, Promise<R>>() {
            @Override
            public Promise<R> apply(final Promise<T> promiseT, final Promise<U> promiseU) {
                Preconditions.checkNotNull(promiseT, "promise cannot be null");
                return promiseT.join(new Function<T, Promise<R>>() {

                    @Nullable
                    @Override
                    public Promise<R> apply(@Nullable T t) {
                        return promiseU.then(function.apply(t));
                    }
                });
            }
        };
    }



    public static <T, R> Promise<List<R>> map(Iterable<T> items, Function<T, Promise<R>> function) {

        List<Promise<List<R>>> promisedItems = Lists.newArrayList();
        for(T item : items) {

            promisedItems.add(function.apply(item).then(Functions2.<R>singletonList()));
        }

        // we need a concat function that will take two [ Promise<List<R>> Promise<List<R>> -> Promise<List<R>> ]\
        BiFunction<Promise<List<R>>, Promise<List<R>>, Promise<List<R>>> concatOp = fmap(new ConcatList<R>());
        Promise<List<R>> initialValue = Promise.<List<R>>resolved(new ArrayList<R>());


        return BiFunctions.foldLeft(initialValue, concatOp, promisedItems);
    }

    /**
     * Convenience function for applying an fmap'd foldLeft to a list of Promises.
     */
    public static <T> Promise<T> foldLeft(T initialValue, BiFunction<T, T, T> operator, Iterable<Promise<T>> promises) {
        return BiFunctions.foldLeft(Promise.resolved(initialValue), fmap(operator), promises);
    }

    /**
     * Convenience function for concatenating a promised item with a promised list of items of the same type
     */
    public static <T> Promise<List<T>> prepend(Promise<T> a, Promise<List<T>> b) {
        Promise<List<T>> aList = a.then(Functions2.<T>singletonList());
        return fmap(new ConcatList<T>()).apply(aList, b);
    }


    /**
     *
     * @param function
     * @param <R>
     * @return
     */
    public <R> Promise<R> then(final Function<? super T, R> function) {
        final Promise<R> chained = new Promise<R>();
        then(new AsyncCallback<T>() {

            @Override
            public void onFailure(Throwable caught) {
                chained.reject(caught);
            }

            @Override
            public void onSuccess(T t) {
                try {
                    chained.resolve(function.apply(t));
                } catch (Throwable caught) {
                    chained.reject(caught);
                }
            }
        });
        return chained;
    }

    public <R> Promise<R> then(final Supplier<R> function) {
        return then(Functions.forSupplier(function));
    }

    public T get() {
        if(state != State.FULFILLED) {
            throw new IllegalStateException();
        }
        return value;
    }

    public Throwable getException() {
        if(state != State.REJECTED) {
            throw new IllegalStateException();
        }
        return exception;
    }

    @Override
    public void onFailure(Throwable caught) {
        reject(caught);
    }

    @Override
    public void onSuccess(T result) {
        resolve(result);
    }

    public final void reject(Throwable caught) {
        if (state != State.PENDING) {
            return;
        }
        this.exception = caught;
        this.state = State.REJECTED;

        publishRejection();
    }

    private void publishRejection() {
        if (callbacks != null) {
            for (AsyncCallback<? super T> callback : callbacks) {
                callback.onFailure(exception);
            }
        }
    }

    private void publishFulfillment() {
        if (callbacks != null) {
            for (AsyncCallback<? super T> callback : callbacks) {
                callback.onSuccess(value);
            }
        }
    }

    public static <T> Promise<T> resolved(T value) {
        Promise<T> promise = new Promise<T>();
        promise.resolve(value);
        return promise;
    }

    public static Promise<Void> done() {
        return Promise.resolved(null);
    }

    public static <X> Promise<X> rejected(Throwable exception) {
        Promise<X> promise = new Promise<X>();
        promise.reject(exception);
        return promise;
    }

    /**
     * Applies an asynchronous function to each of the elements in {@code items},
     * @param items
     * @param function
     * @param <T>
     * @return
     */
    public static <T> Promise<Void> forEach(Iterable<T> items, final Function<? super T, Promise<Void>> function) {
        Promise<Void> promise = Promise.resolved(null);
        for(final T item : items) {
            promise = promise.join(new Function<Void, Promise<Void>>() {
                @Nullable
                @Override
                public Promise<Void> apply(@Nullable Void input) {
                    return function.apply(item);
                }
            });
        }
        return promise;
    }

    public static Promise<Void> waitAll(Promise<?>... promises) {
        return waitAll(Arrays.asList(promises));
    }

    public static Promise<Void> waitAll(final List<? extends Promise<?>> promises) {

        if(promises.isEmpty()) {
            return Promise.done();
        }

        final Promise<Void> result = new Promise<>();
        final int[] remaining = new int[] { promises.size() };
        AsyncCallback callback = new AsyncCallback() {
            @Override
            public void onFailure(Throwable caught) {
                result.onFailure(caught);
            }

            @Override
            public void onSuccess(Object o) {
                remaining[0]--;
                if(remaining[0] == 0) {
                    result.onSuccess(null);
                }
            }
        };
        for(int i=0;i!=promises.size();++i) {
            promises.get(i).then(callback);
        }
        return result;
    }


    @Override
    public String toString() {
        switch(state) {
            case FULFILLED:
                return "<fulfilled: " + value + ">";
            case REJECTED:
                return "<rejected: " + exception.getClass().getSimpleName() + ">";
            default:
            case PENDING:
                return "<pending>";
        }
    }
}
