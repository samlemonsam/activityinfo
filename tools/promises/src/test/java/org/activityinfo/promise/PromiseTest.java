/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

    @Test
    public void map() {


    }
}
