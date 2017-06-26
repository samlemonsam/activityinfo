package org.activityinfo.indexedb;


public interface ObjectStoreDefinition<T> {

    String getName();

    void upgrade(IDBDatabaseUpgrade database, int oldVersion);

    T wrap(IDBObjectStore store);
}
