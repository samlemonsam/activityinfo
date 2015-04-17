package org.activityinfo.promise;

/**
 * Provides a mechanism for receiving push-based notifications.
 * <p>
 * After an Observer calls an {@link Observable}'s {@link Observable#subscribe subscribe} method, the
 * {@code Observable} calls the Observer's {@link #onChange} method to provide notifications.
 *
 * @param <T>
 *          the type of item the Observer expects to observe
 */
public interface Observer<T> {
    
    void onChange(Observable observable);
}
