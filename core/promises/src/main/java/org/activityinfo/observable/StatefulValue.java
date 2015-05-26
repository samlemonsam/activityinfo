package org.activityinfo.observable;

public class StatefulValue<T> extends Observable<T> {
    
    private boolean loading = true;
    private T value;
    
    @Override
    public boolean isLoading() {
        return loading;
    }

    /**
     * Updates the reference to this state's value and notifies subscribers.
     */
    public void updateValue(T value) {
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
        if(!loading) {
            value = null;
            loading = true;
            fireChange();
        };
    }

    @Override
    public T get() {
        if(loading) {
            throw new IllegalStateException();
        }
        return value;
    }
}
