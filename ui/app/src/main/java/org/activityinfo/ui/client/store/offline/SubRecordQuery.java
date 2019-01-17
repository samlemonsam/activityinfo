package org.activityinfo.ui.client.store.offline;

import org.activityinfo.indexedb.OfflineDatabase;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.tasks.SimpleTask;

import java.util.List;

public class SubRecordQuery extends SimpleTask<List<FormRecord>> {

    private final OfflineDatabase executor;
    private final ResourceId formId;
    private final RecordRef parent;

    public SubRecordQuery(OfflineDatabase executor, ResourceId formId, RecordRef parent) {
        this.executor = executor;
        this.formId = formId;
        this.parent = parent;
    }

    @Override
    protected Promise<List<FormRecord>> execute() {
        return executor
                .begin(RecordStore.NAME)
                .query(tx -> tx.objectStore(RecordStore.DEF).get(formId, parent));
    }

}
