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