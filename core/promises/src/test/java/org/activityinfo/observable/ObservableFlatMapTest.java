package org.activityinfo.observable;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ObservableFlatMapTest {

    @Test
    public void addElements() {


        StatefulValue<Integer> a = new StatefulValue<>(41);
        StatefulValue<Integer> b = new StatefulValue<>(42);
        StatefulValue<Integer> c = new StatefulValue<>(43);

        StatefulList<Observable<Integer>> list = new StatefulList<>();
        list.add(a);
        list.add(b);

        Observable<List<Integer>> flatList = Observable.flatten(list);

        CountingObserver<List<Integer>> observer = new CountingObserver<>();
        flatList.subscribe(observer);

        assertThat(observer.countChanges(), equalTo(1));
        assertThat(flatList.get(), contains(41, 42));

        // When changing a single element already in the list, we should get a single change notification
        a.updateValue(81);

        assertThat(observer.countChanges(), equalTo(1));

        // When adding a new item, we should also get a single notification
        list.add(c);

        assertThat(observer.countChanges(), equalTo(1));
        assertThat(flatList.get(), contains(81, 42, 43));


        // Removing the middle item...
        list.removeAt(1);

        assertThat(observer.countChanges(), equalTo(1));
        assertThat(flatList.get(), contains(81, 43));

        // Updating a remove item should not trigger notification
        b.updateValue(82);
        assertThat(observer.countChanges(), equalTo(0));

    }
}