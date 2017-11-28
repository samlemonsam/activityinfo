package org.activityinfo.promise;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Map a function over a list and concatenate the results.
 */
public class ConcatMapFunction<T, R> extends BiFunction<Function<T, R>, Iterable<T>, List<R>> {
    @Override
    public List<R> apply(Function<T, R> function, Iterable<T> items) {
        List<R> list = Lists.newArrayList();
        for(T item : items) {
            list.add(function.apply(item));
        }
        return list;
    }
}
