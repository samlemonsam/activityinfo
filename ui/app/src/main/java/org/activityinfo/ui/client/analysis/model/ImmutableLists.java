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
package org.activityinfo.ui.client.analysis.model;

import com.google.common.base.Function;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ImmutableLists {

    public static <T> List<T> update(List<T> list, T updatedItem, Function<T, String> keyProvider) {

        String updatedId = keyProvider.apply(updatedItem);

        List<T> newList = new ArrayList<>(list.size());
        boolean updated = false;
        for (T item : list) {
            String itemId = keyProvider.apply(item);
            if(itemId.equals(updatedId)) {
                newList.add(updatedItem);
                updated = true;
            } else {
                newList.add(item);
            }
        }

        if (!updated) {
            newList.add(updatedItem);
        }
        return newList;
    }

    public static <T> List<T> remove(List<T> list, String id, Function<T, String> keyProvider) {

        List<T> newList = new ArrayList<>(list.size());
        for (T item : list) {
            String itemId = keyProvider.apply(item);
            if(!itemId.equals(id)) {
                newList.add(item);
            }
        }
        return newList;
    }

    public static <T> List<T> reorder(List<T> list, String afterId, List<T> items, Function<T, String> keyProvider) {

        Set<String> toReorder = new HashSet<>();
        for (T item : items) {
            if(!toReorder.add(keyProvider.apply(item))) {
                throw new IllegalArgumentException("Duplicate ids in items");
            }
        }
        boolean added = false;
        List<T> newList = new ArrayList<>(list.size());
        for (T item : list) {
            String itemId = keyProvider.apply(item);
            if(!toReorder.contains(itemId)) {
                newList.add(item);
            }
            if(itemId.equals(afterId)) {
                newList.addAll(items);
                added = true;
            }
        }
        if(!added) {
            newList.addAll(items);
        }
        return newList;
    }
}
