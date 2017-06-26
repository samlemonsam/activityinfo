package org.activityinfo.indexedb;


import org.activityinfo.indexedb.IDBTransaction;

public interface VoidWork {

    void execute(IDBTransaction tx);

}
