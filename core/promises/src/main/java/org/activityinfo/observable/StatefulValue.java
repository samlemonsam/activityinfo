package org.activityinfo.observable;

import com.google.common.base.Preconditions;

public class StatefulValue<T> extends Observable<T> {
    
    private T value;
    
    @Override
    public boolean isLoading() {
        return value == null;
    }

    /**
     * Updates the reference to this state's value and notifies subscribers.
     */
    public void updateValue(T value) {
        Preconditions.checkNotNull(value, "value cannot be null");
        this.value = value;
        fireChange();
    }

    /**
     * Notify subscribers that this state's value has changed.
     */
    public void updated() {
        fireChange();
    }
    
    public void clear() {
        if(value != null) {
            value = null;
            fireChange();
        };
    }

    @Override
    public T get() {
        if(value == null) {
            throw new IllegalStateException();
        }
        return value;
    }
}
