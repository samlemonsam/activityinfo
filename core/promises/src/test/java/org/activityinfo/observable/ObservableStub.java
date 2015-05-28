package org.activityinfo.observable;


import com.google.common.base.Function;

public class ObservableStub<T> extends Observable<T> {
    
    private boolean loading = true;
    private T value;
    private boolean connected = false;

    public ObservableStub() {
    }

    public ObservableStub(T initialValue) {
        this.loading = false;
        this.value = initialValue;
    }

    public void loading() {
        if(!loading) {
            loading = true;
            fireChange();
        }
    }
    
    public void updateValue(T value) {
        this.loading = false;
        this.value = value;
        fireChange();
    }

    @Override
    protected void onConnect() {
        connected = true;
    }

    @Override
    protected void onDisconnect() {
        connected = false;
    }

    @Override
    public boolean isLoading() {
        return loading;
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    public T get() {
        if(loading) {
            throw new IllegalStateException();
        }
        return value;
    }
}
