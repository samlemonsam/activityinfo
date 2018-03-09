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

/**
 * Represents a function that accepts two arguments and produces a result.
 * This is the two-arity specialization of Function.
 *
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <R> the type of the result of the function
 */
public abstract class BiFunction<T, U, R> implements Function<T, Function<U, R>>, Function2<T, U, R> {

    public abstract R apply(T t, U u);

    @Override
    public final Function<U, R> apply(final T t) {
        return new Function<U, R>() {
            @Override
            public R apply(U u) {
                return BiFunction.this.apply(t, u);
            }
        };
    }
}
