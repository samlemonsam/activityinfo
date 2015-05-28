package org.activityinfo.observable;

import java.util.HashSet;

/**
 * Encapsulates model state represented as a set
 */
public class StatefulSet<T> extends ObservableSet<T> {
    
    private HashSet<T> set = new HashSet<>();
    
    @Override
    public boolean isLoading() {
        return false;
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
