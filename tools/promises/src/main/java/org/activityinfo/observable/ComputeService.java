package org.activityinfo.observable;

import com.google.common.base.Function;


public interface ComputeService {

    <T, U> Observable<U> transform(Observable<T> argument, Function<T, U> function);

}
