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

import com.google.common.base.Function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class ObservableListMap<T, R> extends ObservableList<R> {

    private final ObservableList<T> source;
    private Subscription sourceSubscription = null;

    private final Function<T, R> function;

    private List<R> results = null;
    private Map<T, R> resultMap = null;

    ObservableListMap(ObservableList<T> source, Function<T, R> function) {
        this.source = source;
        this.function = function;
    }

    @Override
    public boolean isLoading() {
        return results == null;
    }


    @Override
    protected void onConnect() {

        sourceSubscription = source.subscribe(new ListObserver<T>() {
            @Override
            public void onChange() {
                rebuildList();
                ObservableListMap.this.fireChanged();
            }

            @Override
            public void onElementAdded(T element) {
                ObservableListMap.this.fireAdded(map(element));
            }

            @Override
            public void onElementRemoved(T element) {
                R resultItem = resultMap.remove(element);
                results.remove(resultItem);
                ObservableListMap.this.fireRemoved(resultItem);
            }
    });
    }

    @Override
    protected void onDisconnect() {
        sourceSubscription.unsubscribe();
    }

    private void rebuildList() {
        if (source.isLoading()) {
            results = null;
            resultMap = null;
        } else {
            results = new ArrayList<>();
            resultMap = new HashMap<>();
            for (T sourceItem : source.getList()) {
                map(sourceItem);
            }
        }
    }

    private R map(T sourceItem) {
        R resultItem = function.apply(sourceItem);
        assert resultItem != null : "result of list map operation was null";
        results.add(resultItem);
        resultMap.put(sourceItem, resultItem);
        return resultItem;
    }


    @Override
    public List<R> getList() {
        assert results != null : "The list is not loaded.";
        List<R> resultList = new ArrayList<>();
        for (R result : results) {
            resultList.add(result);
        }
        return resultList;
    }
}
