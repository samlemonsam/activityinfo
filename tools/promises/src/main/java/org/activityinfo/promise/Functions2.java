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

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Additional static functions that operate on {@link com.google.common.base.Function}
 */
public class Functions2 {


    /**
     * Creates a function which creates a singleton list of its argument.
     *
     * <p>Haskell people would say that this is "unit" function of the List Monad, but
     * somehow "singleton list" is a bit clearer.</p>
     */
    public static <T> Function<T, List<T>> singletonList() {
        return new Function<T, List<T>>() {
            @Override
            public List<T> apply(@Nullable T input) {
                return Collections.singletonList(input);
            }
        };
    }
}
