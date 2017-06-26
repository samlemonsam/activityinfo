package org.activityinfo.indexedb;


import org.activityinfo.promise.Promise;

public interface IDBTransactionCallback<T> {


    void execute(IDBTransaction transaction);

    void onComplete(IDBTransactionEvent event);
    void onError(IDBTransactionEvent event);
    void onAbort(IDBTransactionEvent event);
}
