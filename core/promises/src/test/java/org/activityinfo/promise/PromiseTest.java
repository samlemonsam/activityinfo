package org.activityinfo.promise;

import com.google.common.base.Function;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;


public class PromiseTest {

    @Test
    public void forEach() {

        List<Integer> numbers = Arrays.asList(1,2,3);
        Promise<Void> result = Promise.forEach(numbers, new Function<Integer, Promise<Void>>() {
            @Nullable
            @Override
            public Promise<Void> apply(@Nullable Integer input) {
                return Promise.rejected(new UnsupportedOperationException());
            }
        });

        assertThat(result.getState(), equalTo(Promise.State.REJECTED));
    }


    @Test
    public void normallyResolved() {

        Promise<Integer> promise = new Promise<Integer>();
        assertFalse(promise.isSettled());
        assertThat(promise.getState(), equalTo(Promise.State.PENDING));

        promise.resolve(64);

        assertThat(promise.getState(), equalTo(Promise.State.FULFILLED));
        assertThat(promise, PromiseMatchers.resolvesTo(equalTo(64)));

        Function<Integer, Double> takeSquareRoot = new Function<Integer, Double>() {

            @Nullable
            @Override
            public Double apply(@Nullable Integer integer) {
                return Math.sqrt(integer);
            }
        };

        assertThat(promise.then(takeSquareRoot), PromiseMatchers.resolvesTo(equalTo(8.0)));
    }
}
