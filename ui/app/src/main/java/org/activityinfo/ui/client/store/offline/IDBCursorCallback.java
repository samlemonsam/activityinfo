package org.activityinfo.ui.client.store.offline;

public interface IDBCursorCallback {

    void onNext(IDBCursor cursor);

    void onDone();
}
