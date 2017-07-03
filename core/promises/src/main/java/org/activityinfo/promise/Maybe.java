package org.activityinfo.promise;

import com.google.common.base.Function;
import com.google.common.base.Optional;

import java.util.List;

/**
 * Holds either a value, or is empty because it is FORBIDDEN, DELETED, OR NOT_FOUND.
 */
public class Maybe<T> {


    public enum State {
        VISIBLE,
        FORBIDDEN,
        DELETED,
        NOT_FOUND
    }

    private State state;
    private T value;

    private static final Maybe FORBIDDEN = new Maybe(State.FORBIDDEN, null);
    private static final Maybe DELETED = new Maybe(State.DELETED, null);
    private static final Maybe NOT_FOUND = new Maybe(State.NOT_FOUND, null);

    public static <T> Maybe<T> of(T value) {
        return new Maybe<T>(State.VISIBLE, value);
    }

    public static <T> Maybe<T> notFound() {
        return NOT_FOUND;
    }

    public static <T> Maybe<T> deleted() {
        return DELETED;
    }

    public static <T> Maybe<T> forbidden() {
        return FORBIDDEN;
    }

    private Maybe(State state, T value) {
        this.state = state;
        this.value = value;
    }

    public State getState() {
        return state;
    }


    public boolean isVisible() {
        return value != null;
    }

    public T get() {
        assert state == State.VISIBLE;
        return value;
    }

    public Optional<T> getIfVisible() {
        if(isVisible()) {
            return Optional.of(get());
        } else {
            return Optional.absent();
        }
    }


    @SuppressWarnings("unchecked")
    public <R> Maybe<R> transform(Function<T, R> function) {
        if(isVisible()) {
            return Maybe.of(function.apply(value));
        } else {
            return (Maybe<R>) this;
        }
    }

    public T or(T defaultValue) {
        if(isVisible()) {
            return get();
        } else {
            return defaultValue;
        }
    }

    public static <T> Maybe<T> fromOptional(Optional<T> optional) {
        if(optional.isPresent()) {
            return Maybe.of(optional.get());
        } else {
            return Maybe.notFound();
        }
    }

    @Override
    public int hashCode() {
        if(value == null) {
            return 0;
        } else {
            return value.hashCode();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }

        if(!(obj instanceof Maybe)) {
            return false;
        }
        Maybe other = (Maybe) obj;

        if(this.value != null) {
            return this.value.equals(other.value);
        } else {
            return this.state == other.state;
        }
    }

    @Override
    public String toString() {
        if(state == State.VISIBLE) {
            return "Maybe{" + value + "}";
        } else {
            return "Maybe{" + state + "}";
        }
    }
}
