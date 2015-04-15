package org.activityinfo.service.store;

/**
 * Called by the cursor on each new Resource
 */
public interface CursorObserver<T> {

    /**
     * Called when the Cursor advances to the next Resource
     *
     * @param value the value of the field at the Cursor's new position
     */
    void onNext(T value);

    /**
     * Called when the cursor is closed.
     */
    void done();

}
