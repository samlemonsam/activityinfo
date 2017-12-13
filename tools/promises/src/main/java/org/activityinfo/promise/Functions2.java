package org.activityinfo.promise;

import com.google.common.base.Function;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Additional static functions that operate on {@link com.google.common.base.Function}
 */
public class Functions2 {


    /**
     * Creates a function which creates a singleton list of its argument.
     *
     * <p>Haskell people would say that this is "unit" function of the List Monad, but
     * somehow "singleton list" is a bit clearer.</p>
     */
    public static <T> Function<T, List<T>> singletonList() {
        return new Function<T, List<T>>() {
            @Override
            public List<T> apply(@Nullable T input) {
                return Collections.singletonList(input);
            }
        };
    }
}
