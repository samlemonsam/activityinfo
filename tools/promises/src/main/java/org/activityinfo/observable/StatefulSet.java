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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Encapsulates model state represented as a set
 */
public class StatefulSet<T> extends ObservableSet<T> {
    
    private HashSet<T> set = new HashSet<>();
    
    @Override
    public boolean isLoading() {
        return false;
    }

    @Override
    public Set<T> asSet() {
        return Collections.unmodifiableSet(set);
    }


    public void add(T element) {
        boolean added = set.add(element);
        if(added) {
            fireAdded(element);
        }
    }
    
    public void remove(T element) {
        boolean removed = set.remove(element);
        if(removed) {
            fireRemoved(element);
        }
    }
}
