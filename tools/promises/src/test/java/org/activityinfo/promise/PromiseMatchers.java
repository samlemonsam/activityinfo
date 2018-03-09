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
package org.activityinfo.promise;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Hamcrest matchers for promises
 */
public class PromiseMatchers {

    public static <T> T assertResolves(Promise<T> promise) {
        final List<T> results = new ArrayList<>();
        promise.then(new AsyncCallback<T>() {
            @Override
            public void onFailure(Throwable caught) {
                throw new RuntimeException(caught);
            }

            @Override
            public void onSuccess(T result) {
                // no problems
                results.add(result);
            }
        });
        if(results.size() > 1) {
            throw new RuntimeException("Callback called " + results.size() + " times, expected exactly one callback.");
        }
        if(results.isEmpty()) {
            throw new RuntimeException("Callback not called, expected exactly one callback.");
        }
        return results.get(0);
    }

    public static <T> Matcher<Promise<? extends T>> resolvesTo(final Matcher<T> matcher) {
        return new TypeSafeMatcher<Promise<? extends T>>() {

            private T resolution = null;

            @Override
            public boolean matchesSafely(Promise<? extends T> item) {

                item.then(new AsyncCallback<T>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        throw new AssertionError(throwable);
                    }

                    @Override
                    public void onSuccess(T t) {
                        resolution = t;
                    }
                });

                return matcher.matches(resolution);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("resolves to value ");
                matcher.describeTo(description);
            }
        };
    }
}
