package org.activityinfo.ui.client.store.offline;

import com.google.common.base.Function;
import org.activityinfo.indexedb.OfflineDatabase;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.promise.Maybe;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.tasks.SimpleTask;

import java.util.Optional;

public class RecordQuery extends SimpleTask<Maybe<FormRecord>> {



    private final OfflineDatabase executor;
    private final RecordRef recordRef;

    public RecordQuery(OfflineDatabase executor, RecordRef recordRef) {
        this.recordRef = recordRef;
        this.executor = executor;
    }

    @Override
    protected Promise<Maybe<FormRecord>> execute() {
        return executor
            .begin(RecordStore.NAME)
            .query(tx -> {
                RecordStore recordStore = tx.objectStore(RecordStore.DEF);
                return recordStore.get(recordRef);
            })
            .then(new Function<Optional<RecordObject>, Maybe<FormRecord>>() {
                @Override
                public Maybe<FormRecord> apply(Optional<RecordObject> formRecord) {
                    if(formRecord.isPresent()) {
                        return Maybe.of(formRecord.get().toFormRecord(recordRef));
                    } else {
                        return Maybe.notFound();
                    }
                }
            });
    }
}
