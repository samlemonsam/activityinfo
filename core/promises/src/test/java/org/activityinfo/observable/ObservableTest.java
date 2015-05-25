package org.activityinfo.observable;

import com.google.common.base.Function;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class ObservableTest {

    @Test
    public void subscriptions() {
        
        ObservableStub<Integer> observable = new ObservableStub<>();
        assertFalse(observable.isConnected());
        
        MockObserver<Integer> observer = new MockObserver<>();
        Subscription subscription = observable.subscribe(observer);

        assertTrue(observable.isConnected());
        observer.assertChangeFiredOnce();
        
        observable.updateValue(42);
        observer.assertChangeFiredOnce();
        subscription.unsubscribe();

        // As the last observer disconnects, this observable
        // should transition to disconnected
        assertFalse(observable.isConnected());
        
        observable.updateValue(43);

        // After disconnection, we should not receive any further
        // notifications
        observer.assertChangeNotFired();
    }
    
    @Test
    public void transform() {
        ObservableStub<Integer> number = new ObservableStub<>();
        Observable<Integer> twice = number.transform(new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer input) {
                return input * 2;
            }
        });
        
        MockObserver<Integer> twiceObserver = new MockObserver<>();
        Subscription twiceSubscription = twice.subscribe(twiceObserver);
        twiceObserver.assertChangeFiredOnce();

        assertTrue(number.isLoading());
        assertTrue(twice.isLoading());
        
        number.updateValue(42);

        twiceObserver.assertChangeFiredOnce();
        assertFalse(twice.isLoading());
        assertThat(twice.get(), equalTo(42 * 2));
        
        number.loading();
        twiceObserver.assertChangeFiredOnce();
        assertTrue(twice.isLoading());

        number.updateValue(13);
        twiceObserver.assertChangeFiredOnce();
        assertFalse(twice.isLoading());
        assertThat(twice.get(), equalTo(13 * 2));

        twiceSubscription.unsubscribe();

        number.updateValue(96);
        twiceObserver.assertChangeNotFired();
    }
    
    @Test
    public void chained() {
        final RemoteServiceStub remoteService = new RemoteServiceStub();
        ObservableStub<Integer> id = new ObservableStub<>();
        Observable<String> name = id.join(new Function<Integer, Observable<String>>() {
            @Override
            public Observable<String> apply(Integer input) {
                return remoteService.queryName(input);
            }
        });
        
        MockObserver<String> nameObserver = new MockObserver<>();
        Subscription nameSubscription = name.subscribe(nameObserver);
        nameObserver.assertChangeFiredOnce();

        id.updateValue(42);
        
        // Initially, the value computed on the value from the remote
        // service should also be "loading"
        nameObserver.assertChangeFiredOnce();
        assertTrue(name.isLoading());

        // Once the remote service completes, the calculated value should also be updated
        remoteService.completePending();
        nameObserver.assertChangeFiredOnce();
        assertThat(name.get(), equalTo("name42"));
    }
    
    @Test
    public void chainedConnection() {
        final ObservableStub<Integer> id = new ObservableStub<>(1);
        final ObservableStub<String> remoteValue1 = new ObservableStub<>();
        final ObservableStub<String> remoteValue2 = new ObservableStub<>();

        Observable<String> result = id.join(new Function<Integer, Observable<String>>() {
            @Override
            public Observable<String> apply(Integer input) {
                if(input == 1) {
                    return remoteValue1;
                } else {
                    return remoteValue2;
                }
            }
        });
        
        MockObserver<String> resultObserver = new MockObserver<>();
        result.subscribe(resultObserver);
        assertTrue(remoteValue1.isConnected());

        remoteValue1.updateValue("name1");
        assertThat(result.get(), equalTo("name1"));
        
        
    }
}