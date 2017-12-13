package org.activityinfo.promise;

import com.google.common.base.Function;

import java.util.List;

/**
 * Static utility methods pertaining to {@code BiFunction} instances.
 *
 */
public class BiFunctions {

    public static <T, R> Function<Iterable<T>, List<R>> concatMap(Function<T, R> function) {
        return new ConcatMapFunction<T, R>().apply(function);
    }

    public static <T> T foldLeft(T initialValue, BiFunction<T, T, T> operator, Iterable<T> items) {
        return new FoldLeftFunction<T>(initialValue).apply(operator, items);
    }

}
