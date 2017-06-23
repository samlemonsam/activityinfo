package org.activityinfo.ui.client.store.offline;

/**
 * Callback interface that handles events from {@link IDBCursor} events.
 *
 */
public interface IDBCursorCallback {

    void onNext(IDBCursor cursor);

    void onDone();
}
