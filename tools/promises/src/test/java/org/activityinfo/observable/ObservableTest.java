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

import com.google.common.base.Function;
import org.junit.Test;

import javax.annotation.Nullable;

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
        SchedulerStub scheduler = new SchedulerStub();

        Observable<Integer> twice = number.transform(scheduler, new Function<Integer, Integer>() {
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

        // When we update the source value, the calculated value should
        // remain in the loading state but enqueue the recomputation
        number.updateValue(42);
        assertTrue(twice.isLoading());

        // When then the scheduler runs, the value should be recomputed and 
        // the observer notified with the resulting value
        scheduler.runAll();        
        twiceObserver.assertChangeFiredOnce();
        assertFalse(twice.isLoading());
        assertThat(twice.get(), equalTo(42 * 2));
        
        number.setToLoading();
        twiceObserver.assertChangeFiredOnce();
        assertTrue(twice.isLoading());

        // When the value is changed, we expect the computed value
        // to REMAIN in the loading state, so no change is fired
        number.updateValue(13);
        twiceObserver.assertChangeNotFired();
        assertTrue(twice.isLoading());
        
        // ... and when the scheduler runs, the change event
        // is fired upon recalculation
        scheduler.runAll();
        twiceObserver.assertChangeFiredOnce();
        assertFalse(twice.isLoading());
        assertThat(twice.get(), equalTo(13 * 2));

        twiceSubscription.unsubscribe();

        number.updateValue(96);
        twiceObserver.assertChangeNotFired();
    }


    @Test
    public void transformSynchronous() {
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

        // When we update the source value, the calculated value should
        // remain in the loading state but enqueue the recomputation
        number.updateValue(42);

        twiceObserver.assertChangeFiredOnce();
        assertFalse(twice.isLoading());
        assertThat(twice.get(), equalTo(42 * 2));

        number.setToLoading();
        twiceObserver.assertChangeFiredOnce();
        assertTrue(twice.isLoading());

        // When the value is changed, we expect the computed value
        // to REMAIN in the loading state, so no change is fired
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
    public void joined() {

        StatefulValue<Integer> x = new StatefulValue<>(0);
        Observable<Integer> abs = x.join(new Function<Integer, Observable<Integer>>() {
            @Override
            public Observable<Integer> apply(Integer integer) {
                return Observable.just(Math.abs(integer));
            }
        });

        Observable<Double> sqrt = abs.join(new Function<Integer, Observable<Double>>() {
            @Nullable
            @Override
            public Observable<Double> apply(@Nullable Integer integer) {
               return Observable.just(Math.sqrt(integer));
            }
        });

        CountingObserver<Double> sqrtObserver = new CountingObserver<>();
        sqrt.subscribe(sqrtObserver);

        x.updateIfNotEqual(-16);

        assertThat(sqrtObserver.countChanges(), equalTo(2));


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


    @Test
    public void reentrantConnections() {
        StatefulValue<Integer> x = new StatefulValue<>(3);
        Observable<Integer> z = x.transform(x_ -> x_ * x_);

        z.subscribe(zo1 -> {
            System.out.println("zo1 change");

            z.subscribe(zo2 -> {
                System.out.println("zo2 change");
            });
        });

    }


}
