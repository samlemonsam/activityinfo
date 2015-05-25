package org.activityinfo.observable;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;


public class SameThreadComputeService implements ComputeService {
    @Override
    public <T, R> Observable<R> transform(Observable<T> argument, final Function<T, R> function) {
        return new ObservableFunction<R>(argument) {
            @Override
            @SuppressWarnings("unchecked")
            protected R compute(Object[] arguments) {
                T argumentValue = (T) arguments[0];
                return Preconditions.checkNotNull(function.apply(argumentValue));
            }
        };
    }
}
