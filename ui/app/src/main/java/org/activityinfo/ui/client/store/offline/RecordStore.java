package org.activityinfo.ui.client.store.offline;

import org.activityinfo.indexedb.*;
import org.activityinfo.model.form.UpdatedRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.promise.Promise;

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

    private static final String KEY_SEPARATOR = Character.toString(RecordRef.SEPARATOR);

    private IDBObjectStore<RecordObject> impl;

    RecordStore(IDBObjectStore impl) {
        this.impl = impl;
    }

    public final void put(ResourceId formId, UpdatedRecord record) {
        RecordRef ref = new RecordRef(formId, ResourceId.valueOf(record.getRecordId()));
        impl.put(key(ref), RecordObject.from(record));
    }

    public final void put(RecordRef recordRef, RecordObject recordObject) {
        impl.put(key(recordRef), recordObject);
    }

    public final Promise<Optional<RecordObject>> get(RecordRef ref) {
        return impl.get(key(ref)).then(Optional::ofNullable);
    }

    static String key(RecordRef ref) {
        return ref.toQualifiedString();
    }

    public void openCursor(ResourceId formId, IDBCursorCallback<RecordObject> callback) {

        String lowerBound = formLower(formId);
        String upperBound = formUpper(formId);

        impl.openCursor(lowerBound, upperBound, new IDBCursorCallback<RecordObject>() {
            @Override
            public void onNext(IDBCursor<RecordObject> cursor) {

                LOGGER.info("RecordStore.onNext: " + cursor.getKeyString());

                callback.onNext(cursor);
            }

            @Override
            public void onDone() {
                callback.onDone();
            }
        });
    }

    static String formUpper(ResourceId formId) {
        return formId.asString() + KEY_SEPARATOR + "\uFFFF";
    }

    static String formLower(ResourceId formId) {
        return formId.asString() + KEY_SEPARATOR;
    }

    public static ResourceId recordIdOf(IDBCursor<RecordObject> cursor) {
        return RecordRef.fromQualifiedString(cursor.getKeyString()).getRecordId();
    }

    public void deleteRecord(RecordRef recordRef) {
        impl.delete(key(recordRef));
    }

    public void deleteAllRecords(ResourceId formId) {
        impl.delete(formLower(formId), formUpper(formId));
    }



}
