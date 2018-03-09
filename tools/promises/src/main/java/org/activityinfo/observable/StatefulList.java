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
package org.activityinfo.observable;

import com.google.common.base.Predicate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates model state represented as a set
 */
public class StatefulList<T> extends ObservableList<T> {

    private List<T> list = new ArrayList<>();

    @Override
    public boolean isLoading() {
        return false;
    }

    public void add(T item) {
        list.add(item);
        fireAdded(item);
    }

    public void set(T keepItem) {
        list.clear();
        list.add(keepItem);

        fireChanged();
    }

    /**
     * Removes the first element that matches the given {@code predicate}.
     *
     * @return true if a matching element was found and removed.
     */
    public boolean removeFirst(Predicate<T> predicate) {
        for (int i = 0; i < list.size(); i++) {
            if(predicate.apply(list.get(i))) {
                removeAt(i);
                return true;
            }
        }
        return false;
    }

    public T removeAt(int index) {
        T removed = list.remove(index);
        fireRemoved(removed);
        return removed;
    }

    public void clear() {
        list.clear();
        fireChanged();
    }

    @Override
    public List<T> getList() {
        return Collections.unmodifiableList(list);
    }
}
