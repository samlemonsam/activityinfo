package org.activityinfo.promise;

import com.google.common.base.Function;

/**
 * Represents a function that accepts two arguments and produces a result.
 * This is the two-arity specialization of Function.
 *
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <R> the type of the result of the function
 */
public abstract class BiFunction<T, U, R> implements Function<T, Function<U, R>>, Function2<T, U, R> {

    public abstract R apply(T t, U u);

    @Override
    public final Function<U, R> apply(final T t) {
        return new Function<U, R>() {
            @Override
            public R apply(U u) {
                return BiFunction.this.apply(t, u);
            }
        };
    }
}
