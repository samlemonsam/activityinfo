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

import com.google.common.collect.Lists;
import org.activityinfo.promise.BiFunction;

import java.util.List;

/**
 * Binary operator which concatenates two Lists
 */
public class ConcatList<T> extends BiFunction<List<T>, List<T>, List<T>>
{
    @Override
    public List<T> apply(List<T> x, List<T> y) {
        if(x == null) {
            return y;
        } else {
            List<T> result = Lists.newArrayList();
            result.addAll(x);
            result.addAll(y);
            return result;
        }
    }
}
