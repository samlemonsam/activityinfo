package org.activityinfo.indexedb;

/**
 * Handles request to open a database.
 */
public interface IDBOpenDatabaseCallback {

    void onUpgradeNeeded(IDBDatabaseUpgrade database, int oldVersion);

    void onSuccess(IDBDatabase database);

    void onError(IDBOpenDatabaseEvent event);

}
