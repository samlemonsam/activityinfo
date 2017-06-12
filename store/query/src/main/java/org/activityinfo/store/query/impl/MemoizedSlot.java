package org.activityinfo.store.query.impl;

import com.google.common.base.Function;

public class MemoizedSlot<T, R> implements Slot<R> {

    private final Slot<T> input;
    private final Function<T, R> function;

    private R result;

    public MemoizedSlot(Slot<T> input, Function<T, R> function) {
        this.input = input;
        this.function = function;
    }

    @Override
    public R get() {
        if(result == null) {
            result = function.apply(input.get());
        }
        return result;
    }
}
