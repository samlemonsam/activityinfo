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
