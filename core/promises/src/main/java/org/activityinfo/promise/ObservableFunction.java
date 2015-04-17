package org.activityinfo.promise;

public abstract class ObservableFunction<T> extends Observable<T> {

    private final Observable[] arguments;
    private final Subscription[] subscriptions;
    private T value;

    protected ObservableFunction(Observable... arguments) {
        this.arguments = arguments;
        this.subscriptions = new Subscription[arguments.length];
    }

    @Override
    public final boolean isLoading() {
        for(Observable argument : arguments) {
            if(!argument.isLoading()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final T getValue() {
        return value;
    }

    @Override
    protected void onConnect() {
        for(int i=0;i<subscriptions.length;++i) {
            assert subscriptions[i] == null;
            subscriptions[i] = arguments[i].subscribe(new Observer() {
                @Override
                public void onChange(Observable observable) {
                    Object[] argumentValues = new Object[arguments.length];
                    for(int i=0;i<argumentValues.length;++i) {
                        argumentValues[i] = arguments[i].getValue();
                    }
                    value = compute(argumentValues);
                }
            });
        }
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
