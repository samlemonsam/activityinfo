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
package org.activityinfo.store.query.shared;

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
