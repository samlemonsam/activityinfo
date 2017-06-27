package org.activityinfo.indexedb;

import org.activityinfo.indexedb.*;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.offline.KeyValueStore;
import org.activityinfo.ui.client.store.offline.RecordObject;
import org.activityinfo.ui.client.store.offline.RecordStore;
import org.activityinfo.ui.client.store.offline.SchemaStore;

import java.util.*;


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
