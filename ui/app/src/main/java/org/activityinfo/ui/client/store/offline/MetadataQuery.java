package org.activityinfo.ui.client.store.offline;

import org.activityinfo.indexedb.OfflineDatabase;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.tasks.SimpleTask;

public class MetadataQuery extends SimpleTask<FormMetadataObject> {


    private ResourceId formId;
    private OfflineDatabase executor;

    public MetadataQuery(OfflineDatabase executor, ResourceId formId) {
        this.formId = formId;
        this.executor = executor;
    }

    @Override
    protected Promise<FormMetadataObject> execute() {
        return this.executor.begin(FormMetadataStore.DEF)
            .query(tx -> tx.objectStore(FormMetadataStore.DEF).get(formId));
    }
}
