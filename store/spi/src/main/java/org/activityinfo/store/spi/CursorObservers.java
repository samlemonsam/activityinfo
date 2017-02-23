package org.activityinfo.store.spi;


import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.List;

public class CursorObservers {


    /**
     * Creates a new CursorObserver that transforms each item and then forwards it to a target listener.
     */
    public static <F, T> CursorObserver<F> transform(final Function<? super F, T> function, final CursorObserver<T> target) {
        return new CursorObserver<F>() {
            @Override
            public void onNext(F value) {
                target.onNext(function.apply(value));
            }

            @Override
            public void done() {
                target.done();
            }
        };
    }
    
    
    public static <T> List<CursorObserver<T>> collect(final CursorObserver<T> target, int count, Function<List<T>, T> function) {
        Collector<T> collector = new Collector<>(target, count, function);
        List<CursorObserver<T>> observers = Lists.newArrayList();
        for (int i = 0; i < count; i++) {
                observers.add(new CollectorItem<T>(collector, i));
        }
        return observers;
    }
    
    private static class Collector<T> {
        private final CursorObserver<T> target;
        private final int valueCount;
        private final Function<List<T>, T> function;

        private final List<T> values = Lists.newArrayList();
        private int valuesObserved = 0;
        private boolean done;

        public Collector(CursorObserver<T> target, int valueCount, Function<List<T>, T> function) {
            this.target = target;
            this.valueCount = valueCount;
            this.function = function;

            for (int i = 0; i < valueCount; i++) {
                values.add(null);
            }
        }

        private void done() {
            if(!done) {
                done = true;
                target.done();
            }
        }

        private void onNext(int valueIndex, T value) {
            values.set(valueIndex, value);
            valuesObserved++;
            
            if(valuesObserved == valueCount) {
                valuesObserved = 0;
                target.onNext(function.apply(values));
            }
        }
    }
    
    private static class CollectorItem<T> implements CursorObserver<T> {

        private final Collector<T> collector;
        private final int index;

        public CollectorItem(Collector<T> collector, int index) {
            this.index = index;
            this.collector = collector;
        }

        @Override
        public void onNext(T value) {
            collector.onNext(index, value);
        }

        @Override
        public void done() {
            collector.done();
        }
    }
}
