package org.activityinfo.indexedb;

/**
 * The IDBFactory interface of the IndexedDB API lets applications asynchronously access the indexed databases.
 */
public interface IDBFactory {

    void open(String databaseName, int version, IDBOpenDatabaseCallback callback);

}
