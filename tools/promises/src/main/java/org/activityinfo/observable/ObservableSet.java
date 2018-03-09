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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class ObservableSet<T>  {

    private final List<SetObserver<T>> observers = new ArrayList<>();
    
    public abstract boolean isLoading();
    
    public final Subscription subscribe(final SetObserver<T> observer) {
        
        observers.add(observer);
        
        return new Subscription() {
            @Override
            public void unsubscribe() {
                observers.remove(observer);
            }
        };
    }
    
    /**
     * Signal that the given {@code element} has been added to the set
     */
    protected final void fireChanged() {
        for (SetObserver<T> observer : observers) {
            observer.onChange();
        }
    }    
    
    
    /**
     * Signal that the given {@code element} has been added to the set
     */
    protected final void fireAdded(T element) {
        for (SetObserver<T> observer : observers) {
            observer.onElementAdded(element);
        }
    }

    /**
     * Signal that the given {@code element} has been removed from the set
     */
    protected final void fireRemoved(T element) {
        for (SetObserver<T> observer : observers) {
            observer.onElementRemoved(element);
        }
    }
    
    public abstract Set<T> asSet();

    public final Observable<Set<T>> asObservable() {
        return new Observable<Set<T>>() {
            
            private Subscription subscription;
            
            @Override
            public boolean isLoading() {
                return ObservableSet.this.isLoading();
            }

            @Override
            public Set<T> get() {
                return ObservableSet.this.asSet();
            }

            @Override
            protected void onConnect() {
                final Observable<Set<T>> thisObservable = this;
                subscription = ObservableSet.this.subscribe(new SetObserver<T>() {
                    @Override
                    public void onChange() {
                        thisObservable.fireChange();
                    }

                    @Override
                    public void onElementAdded(T element) {
                        thisObservable.fireChange();
                    }

                    @Override
                    public void onElementRemoved(T element) {
                        thisObservable.fireChange();
                    }
                });
            }

            @Override
            protected void onDisconnect() {
                subscription.unsubscribe();
            }
        };
    }

}
