package org.activityinfo.promise;

import org.activityinfo.promise.BiFunction;

/**
 * Takes a binary operator, a starting value (usually some kind of ‘zero’),
 * and an {@code Iterable}.
 *
 * <p>The function is applied to the starting value and the first element of the list,
 * then the result of that and the second element of the list, then the result of that and the
 * third element of the list, and so on.
 */
public class FoldLeftFunction<T> extends BiFunction<BiFunction<T, T, T>, Iterable<T>, T> {

    private final T initialValue;

    public FoldLeftFunction(T initialValue) {
        this.initialValue = initialValue;
    }

    @Override
    public T apply(BiFunction<T, T, T> operator, Iterable<T> items) {
        T value = initialValue;
        for(T item : items) {
            value = operator.apply(value, item);
        }
        return value;
    }
}
