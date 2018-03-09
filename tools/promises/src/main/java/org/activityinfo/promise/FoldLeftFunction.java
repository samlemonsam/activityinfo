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

import org.activityinfo.promise.BiFunction;

/**
 * Takes a binary operator, a starting value (usually some kind of ‘zero’),
 * and an {@code Iterable}.
 *
 * <p>The function is applied to the starting value and the first element of the list,
 * then the result of that and the second element of the list, then the result of that and the
 * third element of the list, and so on.
 */
public class FoldLeftFunction<T> extends BiFunction<BiFunction<T, T, T>, Iterable<T>, T> {

    private final T initialValue;

    public FoldLeftFunction(T initialValue) {
        this.initialValue = initialValue;
    }

    @Override
    public T apply(BiFunction<T, T, T> operator, Iterable<T> items) {
        T value = initialValue;
        for(T item : items) {
            value = operator.apply(value, item);
        }
        return value;
    }
}
