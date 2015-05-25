package org.activityinfo.observable;

import com.google.common.base.Optional;

public abstract class ObservableFunction<T> extends Observable<T> {

    private final Observable[] arguments;
    private final Subscription[] subscriptions;
    private Optional<T> value = Optional.absent();
    
    protected ObservableFunction(Observable... arguments) {
        this.arguments = arguments;
        this.subscriptions = new Subscription[arguments.length];
        computeValue();
    }

    @Override
    public final boolean isLoading() {
        return !value.isPresent();
    }

    @Override
    public final T get() {
        return value.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onConnect() {
        for(int i=0;i<subscriptions.length;++i) {
            assert subscriptions[i] == null;
            subscriptions[i] = arguments[i].subscribe(new Observer() {
                @Override
                public void onChange(Observable observable) {
                    computeValue();
                    ObservableFunction.this.fireChange();
                }
            });
        }
    }

    private void computeValue() {
        value = Optional.absent();
        Object[] argumentValues = new Object[arguments.length];
        for(int i=0;i<argumentValues.length;++i) {
            if(arguments[i].isLoading()) {
                return;
            }
            argumentValues[i] = arguments[i].get();
        }
        value = Optional.of(compute(argumentValues));
    }

    protected abstract T compute(Object[] arguments);

    @Override
    protected void onDisconnect() {
        for(int i=0;i<subscriptions.length;++i) {
            subscriptions[i].unsubscribe();
            subscriptions[i] = null;
        }
    }
}
