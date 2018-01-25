package org.activityinfo.indexedb;

import org.activityinfo.promise.Promise;

import java.util.HashMap;
import java.util.Map;


public class IDBFactoryStub implements IDBFactory {


    private final Map<String, IDBDatabaseStub> databaseMap = new HashMap<>();


    @Override
    public void open(String databaseName, int version, IDBOpenDatabaseCallback callback) {
        IDBDatabaseStub db = databaseMap.get(databaseName);
        if(db == null) {
            db = new IDBDatabaseStub(databaseName);
            databaseMap.put(databaseName, db);
        }

        db.maybeUpgrade(version, callback);

        callback.onSuccess(db);
    }

    @Override
    public Promise<Void> deleteDatabase(String name) {
        throw new UnsupportedOperationException();
    }

}
