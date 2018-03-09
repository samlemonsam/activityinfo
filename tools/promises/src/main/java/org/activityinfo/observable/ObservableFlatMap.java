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

import java.util.*;


class ObservableFlatMap<T> extends Observable<List<T>> {

    private final ObservableList<Observable<T>> source;
    private List<T> list;

    private boolean listFiring = false;
    private Subscription listSubscription = null;
    private Map<Observable<T>, Subscription> subscriptionMap = null;

    ObservableFlatMap(ObservableList<Observable<T>> source) {
        this.source = source;
    }

    @Override
    protected void onConnect() {

        try {
            // Don't fire element changes while we are firing list changes.
            listFiring = true;

            subscriptionMap = new HashMap<>();
            if (!source.isLoading()) {
                for (Observable<T> observable : source.getList()) {
                    observeElement(observable);
                }
            }
        } finally {
            listFiring = false;
        }

        listSubscription = source.subscribe(new ListObserver<Observable<T>>() {
            @Override
            public void onChange() {
                try {
                    listFiring = true;
                    rebuildSubscriptions();
                } finally {
                    listFiring = false;
                }
                rebuildList();
                ObservableFlatMap.this.fireChange();
            }

            @Override
            public void onElementAdded(Observable<T> element) {
                try {
                    listFiring = true;
                    observeElement(element);
                } finally {
                    listFiring = false;
                }
                rebuildList();
                ObservableFlatMap.this.fireChange();
            }

            @Override
            public void onElementRemoved(Observable<T> element) {
                if(subscriptionMap != null) {
                    subscriptionMap.remove(element).unsubscribe();
                }
                rebuildList();
                ObservableFlatMap.this.fireChange();
            }
        });
    }

    private void observeElement(Observable<T> observable) {
        Subscription subscription = observable.subscribe(new ElementObserver<T>());
        subscriptionMap.put(observable, subscription);
    }

    @Override
    protected void onDisconnect() {
        listSubscription.unsubscribe();
        listSubscription = null;

        for (Subscription subscription : subscriptionMap.values()) {
            subscription.unsubscribe();
        }
        subscriptionMap = null;
    }



    private void rebuildSubscriptions() {
        if(subscriptionMap == null) {
            // Nothing to do if no one is listening...
            return;
        }

        if(source.isLoading()) {
            // If the contents of the list is loading, then disconnect from
            // all elements.
            for (Subscription subscription : subscriptionMap.values()) {
                subscription.unsubscribe();
            }
            subscriptionMap.clear();

        } else {
            // If the list elements are loaded, listen to new items, and
            // unsubscribe from elements no longer present.

            List<Observable<T>> elements = source.getList();
            Set<Observable<T>> removed = new HashSet<>(subscriptionMap.keySet());
            for (Observable<T> observable : elements) {
                if(!subscriptionMap.containsKey(observable)) {
                    observeElement(observable);
                } else {
                    removed.remove(observable);
                }
            }
            for (Observable<T> observable : removed) {
                subscriptionMap.remove(observable).unsubscribe();
            }
        }
    }

    private void rebuildList() {
        list = null;
        if(source.isLoading()) {
            return;
        }
        List<T> newList = new ArrayList<>();
        for (Observable<T> observableItem : source.getList()) {
            if(observableItem.isLoading()) {
                return;
            }
            newList.add(observableItem.get());
        }
        this.list = newList;
    }

    @Override
    public boolean isLoading() {
        return list == null;
    }

    @Override
    public List<T> get() {
        assert list != null : "not loaded";
        return list;
    }

    private class ElementObserver<T> implements Observer<T> {
        @Override
        public void onChange(Observable<T> observable) {
            if(!listFiring) {
                rebuildList();
                ObservableFlatMap.this.fireChange();
            }
        }
    }
}
