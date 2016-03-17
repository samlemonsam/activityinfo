package org.activityinfo.observable;

import com.google.common.base.Optional;

import java.util.List;

public abstract class ObservableFunction<T> extends Observable<T> {

    private Scheduler scheduler;
    private final Observable[] arguments;
    private final Subscription[] subscriptions;
    private Optional<T> value = Optional.absent();
    
    private int lastUpdate = 0;
    
    public ObservableFunction(Scheduler scheduler, Observable... arguments) {
        this.scheduler = scheduler;
        this.arguments = arguments;
        this.subscriptions = new Subscription[arguments.length];
        computeValue();
    }

    public ObservableFunction(Scheduler scheduler, List<Observable<?>> arguments) {
        this(scheduler, arguments.toArray(new Observable[arguments.size()]));
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
                }
            });
        }
    }

    private void computeValue() {

        lastUpdate ++;
        
        /*
         * Clear the current value, setting our state to "Loading..." and notify observers
         */
        if(value.isPresent()) {
            value = Optional.absent();
            ObservableFunction.this.fireChange();
        }
        
        final int thisUpdate = lastUpdate;
        
        /*
         * Schedule the re-computation using our provided scheduler.
         */
        scheduler.schedule(new Runnable() {

            @Override
            public void run() {
                Object[] argumentValues = evaluateArguments();
                
                Optional<T> result;

                if (argumentValues == null) {
                    result = Optional.absent();
                } else {
                    result = Optional.of(compute(argumentValues));
                }
                
                if(thisUpdate == lastUpdate) {
                    if(result.isPresent()) {
                        value = result;
                        fireChange();
                    } else {
                        // if the result is "Loading", then only fire if this is a change.
                        if(!isLoading()) {
                            value = result;
                            fireChange();
                        }
                    }
                }
            }

            /**
             * 
             * @return an array of argument values, or {@code null} if any 
             * of the arguments are loading.
             */
            private Object[] evaluateArguments() {
                Object[] argumentValues = new Object[arguments.length];
                for(int i=0;i<argumentValues.length;++i) {
                    if(arguments[i].isLoading()) {
                        return null;
                    }
                    argumentValues[i] = arguments[i].get();
                }
                return argumentValues;
            }
        });
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
