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
    public Set<T> get() {
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
