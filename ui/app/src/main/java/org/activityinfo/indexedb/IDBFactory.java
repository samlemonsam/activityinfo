package org.activityinfo.indexedb;

import org.activityinfo.promise.Promise;

/**
 * The IDBFactory interface of the IndexedDB API lets applications asynchronously access the indexed databases.
 */
public interface IDBFactory {

    void open(String databaseName, int version, IDBOpenDatabaseCallback callback);

    Promise<Void> deleteDatabase(String name);

}
