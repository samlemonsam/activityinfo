package org.activityinfo.indexedb;

import org.activityinfo.promise.Promise;

public interface Work<T> {

    Promise<T> query(IDBTransaction transaction);

}

