package org.activityinfo.observable;

import java.util.Set;

/**
 * Provides a mechanism for receiving push-based notifications of changes to a {@code Set}.
 * <p>
 * After an Observer calls an {@link org.activityinfo.observable.Observable}'s {@link org.activityinfo.observable.Observable#subscribe subscribe} method, the
 * {@code Observable} calls the Observer's {@link #onChange} method to provide notifications.
 *
 * @param <T>
 *          the type of item the Observer expects to observe
 */
public interface SetObserver<T> {
    
    void onChange();
    
    void onElementAdded(T element);
    
    void onElementRemoved(T element);
    
}
