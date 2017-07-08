package org.activityinfo.ui.client.store.offline;

import org.activityinfo.indexedb.OfflineDatabase;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormPermissions;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.tasks.SimpleTask;

import java.util.logging.Logger;


public class MetadataQuery extends SimpleTask<FormMetadata> {

    private static final Logger LOGGER = Logger.getLogger(MetadataQuery.class.getName());

    private ResourceId formId;
    private OfflineDatabase executor;

    public MetadataQuery(OfflineDatabase executor, ResourceId formId) {
        this.formId = formId;
        this.executor = executor;
    }

    @Override
    protected Promise<FormMetadata> execute() {
        return this.executor.begin(SchemaStore.DEF)
        .query(tx -> tx.objectStore(SchemaStore.DEF).get(formId))
        .then(formClass -> {
            if (formClass.isPresent()) {
                FormClass schema = formClass.get();
                return FormMetadata.of(schema.getSchemaVersion(), schema, FormPermissions.full());
            } else {
                throw new IllegalStateException("FormSchema entry is missing for " + formId);
            }
        });
    }

    @Override
    public int refreshInterval(FormMetadata result) {
        return 0;
    }
}
