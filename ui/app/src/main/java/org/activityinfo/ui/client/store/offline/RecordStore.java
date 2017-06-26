package org.activityinfo.ui.client.store.offline;

import org.activityinfo.indexedb.*;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.promise.Promise;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * IndexedDB object store for individual form records.
 */
public class RecordStore {

    public static final ObjectStoreDefinition<RecordStore> DEF = new ObjectStoreDefinition<RecordStore>() {
        @Override
        public String getName() {
            return "records";
        }

        @Override
        public void upgrade(IDBDatabaseUpgrade database, int oldVersion) {
            if(oldVersion < 1) {
                database.createObjectStore(NAME, ObjectStoreOptions.withDefaults());
            }
        }

        @Override
        public RecordStore wrap(IDBObjectStore store) {
            return new RecordStore(store);
        }
    };


    private static final Logger LOGGER = Logger.getLogger(RecordStore.class.getName());


    public static final String NAME = "records";

    private IDBObjectStore<RecordObject> impl;

    RecordStore(IDBObjectStore impl) {
        this.impl = impl;
    }

    public final void put(FormRecord record) {
        impl.put(key(record.getRef()), RecordObject.from(record));
    }

    public final void put(RecordRef recordRef, RecordObject recordObject) {
        impl.put(key(recordRef), recordObject);
    }

    public final Promise<Optional<RecordObject>> get(RecordRef ref) {
        return impl.get(key(ref)).then(Optional::ofNullable);
    }

    private String[] key(RecordRef ref) {
        return new String[] { ref.getFormId().asString(), ref.getRecordId().asString() };
    }

    public void openCursor(ResourceId formId, IDBCursorCallback<RecordObject> callback) {

        String[] lowerBound = new String[] { formId.asString(), "" };
        String[] upperBound = new String[] { formId.asString(), "\uFFFF" };

        impl.openCursor(lowerBound, upperBound, new IDBCursorCallback<RecordObject>() {
            @Override
            public void onNext(IDBCursor<RecordObject> cursor) {

                LOGGER.info("RecordStore.onNext: " + Arrays.toString(cursor.getKeyArray()));

                callback.onNext(cursor);
            }

            @Override
            public void onDone() {
                callback.onDone();
            }
        });
    }


    public static ResourceId recordIdOf(IDBCursor<RecordObject> cursor) {
        return ResourceId.valueOf(cursor.getKeyArray()[1]);
    }


    public void deleteRecord(RecordRef recordRef) {
        impl.delete(key(recordRef));
    }
}
