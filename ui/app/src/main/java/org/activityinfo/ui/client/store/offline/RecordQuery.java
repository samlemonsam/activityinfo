package org.activityinfo.ui.client.store.offline;

import com.google.common.base.Function;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.promise.Maybe;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.tasks.SimpleTask;

import java.util.Optional;

public class RecordQuery extends SimpleTask<Maybe<FormRecord>> {
    private final IDBExecutor executor;
    private final RecordRef recordRef;

    public RecordQuery(IDBExecutor executor, RecordRef recordRef) {
        this.recordRef = recordRef;
        this.executor = executor;
    }

    @Override
    protected Promise<Maybe<FormRecord>> execute() {
        return executor
            .begin(RecordStore.NAME)
            .query(tx -> tx.records().get(recordRef))
            .then(new Function<Optional<FormRecord>, Maybe<FormRecord>>() {
                @Override
                public Maybe<FormRecord> apply(Optional<FormRecord> formRecord) {
                    if(formRecord.isPresent()) {
                        return Maybe.of(formRecord.get());
                    } else {
                        return Maybe.notFound();
                    }
                }
            });
    }
}
