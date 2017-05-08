package org.activityinfo.ui.client.store.offline;


import org.activityinfo.promise.Promise;

public interface Work<T> {

    Promise<T> query(IDBTransaction transaction);
}
