package org.activityinfo.observable;

import org.activityinfo.observable.Observable;

/**
 * Provides a mechanism for receiving push-based notifications.
 * <p>
 * After an Observer calls an {@link org.activityinfo.observable.Observable}'s {@link org.activityinfo.observable.Observable#subscribe subscribe} method, the
 * {@code Observable} calls the Observer's {@link #onChange} method to provide notifications.
 *
 * @param <T>
 *          the type of item the Observer expects to observe
 */
public interface Observer<T> {
    
    void onChange(Observable<T> observable);
}
