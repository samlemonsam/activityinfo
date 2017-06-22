package org.activityinfo.ui.client.store.offline;

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

    private static final Logger LOGGER = Logger.getLogger(RecordStore.class.getName());

    public interface RecordCursor {

        ResourceId getRecordId();

        FormRecord getValue();

        void continue_();
    }

    public interface RecordCursorCallback {

        void onNext(RecordCursor cursor);

        void onDone();

    }


    public static final String NAME = "records";

    private IDBObjectStore impl;

    RecordStore(IDBObjectStore impl) {
        this.impl = impl;
    }

    public final void put(FormRecord record) {
        impl.putJson(record.toJsonElement().toString());
    }

    public final Promise<Optional<FormRecord>> get(RecordRef ref) {
        return impl
        .getJson(key(ref))
        .then(json -> {
            if(json == null) {
                return Optional.empty();
            } else {
                return Optional.of(FormRecord.fromJson(json));
            }
        });
    }

    private String[] key(RecordRef ref) {
        return new String[] { ref.getFormId().asString(), ref.getRecordId().asString() };
    }

    public void openCursor(ResourceId formId, RecordCursorCallback callback) {

        String[] lowerBound = new String[] { formId.asString(), "" };
        String[] upperBound = new String[] { formId.asString(), "\uFFFF" };

        impl.openKeyCursor(lowerBound, upperBound, new IDBCursorCallback() {
            @Override
            public void onNext(IDBCursor cursor) {

                LOGGER.info("RecordStore.onNext: " + Arrays.toString(cursor.getKeyArray()));

                callback.onNext(new RecordCursor() {
                    @Override
                    public ResourceId getRecordId() {
                        return ResourceId.valueOf(cursor.getKeyArray()[1]);
                    }

                    @Override
                    public FormRecord getValue() {
                        return FormRecord.fromJson(cursor.getValueAsJson());
                    }

                    @Override
                    public void continue_() {
                        cursor.continue_();
                    }
                });
            }

            @Override
            public void onDone() {
                callback.onDone();
            }
        });
    }
}
