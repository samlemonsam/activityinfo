package org.activityinfo.store.query.impl;

import org.activityinfo.promise.BiFunction;

public class MemoizedSlot2<X, Y, R> implements Slot<R> {

    private Slot<X> x;
    private Slot<Y> y;
    private BiFunction<X, Y, R> function;

    private R result = null;

    public MemoizedSlot2(Slot<X> x, Slot<Y> y, BiFunction<X, Y, R> function) {
        this.x = x;
        this.y = y;
        this.function = function;
    }

    @Override
    public R get() {
        if(result == null) {
            result = function.apply(x.get(), y.get());
        }
        return result;
    }
}
